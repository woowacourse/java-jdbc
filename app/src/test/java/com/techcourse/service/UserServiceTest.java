package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-service-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;");
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                "gugu", "password", "hkkang@woowahan.com"
        );
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(transactionTemplate, new AppUserService(userDao, userHistoryDao));

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        assertThat(password()).isEqualTo(newPassword);
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ? and password = ? and created_by = ? and created_at is not null",
                rs -> rs.getInt(1), 1L, newPassword, createBy
        );
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(transactionTemplate, new AppUserService(userDao, userHistoryDao));

        final var originalPassword = password();
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        assertThat(userService.findById(1L).getPassword()).isEqualTo(originalPassword);
        assertThat(historyCount()).isZero();
    }

    @AfterEach
    void 트랜잭션_종료_후_연결_바인딩이_남지_않는다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 저장과_조회는_애플리케이션_서비스에_위임한다() {
        final UserService userService = new TxUserService(transactionTemplate,
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        userService.save(new User("lucky", "password", "lucky@example.com"));

        assertThat(userService.findById(2L).getAccount()).isEqualTo("lucky");
    }

    private String password() {
        return jdbcTemplate.queryForObject(
                "select password from users where id = ?", rs -> rs.getString("password"), 1L
        );
    }

    private int historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getInt(1));
    }
}
