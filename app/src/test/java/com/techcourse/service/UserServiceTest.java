package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private JdbcDataSource dataSource;
    private Connection keepAlive;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private long userId;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID());
        keepAlive = dataSource.getConnection();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        userId = userDao.findByAccount("gugu").getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        keepAlive.close();
    }

    @Test
    void 비밀번호_변경과_이력을_함께_커밋한다() {
        final var appUserService = new AppUserService(userDao, new UserHistoryDao(jdbcTemplate));
        final var userService = new TxUserService(dataSource, appUserService);

        userService.changePassword(userId, "newPassword", "admin");

        assertThat(userService.findById(userId).getPassword()).isEqualTo("newPassword");
        final var history = jdbcTemplate.queryForObject("select * from user_history where user_id = ?",
                (resultSet, rowNum) -> resultSet.getString("password") + ":" + resultSet.getString("created_by"),
                userId);
        assertThat(history).isEqualTo("newPassword:admin");
    }

    @Test
    void 이력_저장_이후_예외가_발생하면_비밀번호와_이력을_모두_롤백한다() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        final var userService = new TxUserService(dataSource, appUserService);

        assertThatThrownBy(() -> userService.changePassword(userId, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class);

        assertThat(userService.findById(userId).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void 실제_이력_INSERT가_실패해도_비밀번호를_롤백한다() {
        final var appUserService = new AppUserService(userDao, new UserHistoryDao(jdbcTemplate));
        final var userService = new TxUserService(dataSource, appUserService);

        assertThatThrownBy(() -> userService.changePassword(userId, "newPassword", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(userService.findById(userId).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void 트랜잭션_서비스로_감싸지_않으면_앞서_실행한_변경이_롤백되지_않는다() {
        final var appUserService = new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> appUserService.changePassword(userId, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class);

        assertThat(appUserService.findById(userId).getPassword()).isEqualTo("newPassword");
        assertThat(historyCount()).isOne();
    }

    private long historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history",
                (resultSet, rowNum) -> resultSet.getLong(1));
    }
}
