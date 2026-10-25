package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var h2DataSource = new JdbcDataSource();
        h2DataSource.setURL(
                "jdbc:h2:mem:service-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1"
        );
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        this.dataSource = h2DataSource;

        DatabasePopulatorUtils.execute(dataSource);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        userDao.insert(new User(
                "gugu",
                "password",
                "hkkang@woowahan.com"
        ));
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(
                userDao,
                userHistoryDao,
                dataSource
        );

        final var user = userDao.findByAccount("gugu");
        final var newPassword = "qqqqq";
        final var createdBy = "gugu";

        userService.changePassword(user.getId(), newPassword, createdBy);

        final var actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);

        final var historyCount = jdbcTemplate.queryForObject(
                """
                select count(*) from user_history
                where user_id = ?
                  and password = ?
                  and created_by = ?
                """,
                rs -> rs.getInt(1),
                user.getId(),
                newPassword,
                createdBy
        );

        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(
                userDao,
                userHistoryDao,
                dataSource
        );

        final var user = userDao.findByAccount("gugu");
        final var originalPassword = user.getPassword();
        final var newPassword = "newPassword";

        assertThrows(
                DataAccessException.class,
                () -> userService.changePassword(
                        user.getId(),
                        newPassword,
                        "gugu"
                )
        );

        final var actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(originalPassword);

        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history",
                rs -> rs.getInt(1)
        );

        assertThat(historyCount).isZero();
    }
}
