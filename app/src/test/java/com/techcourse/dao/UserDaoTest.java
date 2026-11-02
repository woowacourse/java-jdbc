package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private DataSource dataSource;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-dao-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;");
        this.dataSource = dataSource;
        DatabasePopulatorUtils.execute(dataSource);

        final var jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                "gugu", "password", "hkkang@woowahan.com"
        );
    }

    @Test
    void findById() throws SQLException {
        try (final var connection = dataSource.getConnection()) {
            final var user = userDao.findById(connection, 1L);

            assertThat(user.getAccount()).isEqualTo("gugu");
        }
    }

    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        try (final var connection = dataSource.getConnection()) {
            final var user = userDao.findById(connection, 1L);
            user.changePassword(newPassword);

            userDao.update(connection, user);

            final var actual = userDao.findById(connection, 1L);

            assertThat(actual.getPassword()).isEqualTo(newPassword);
        }
    }
}
