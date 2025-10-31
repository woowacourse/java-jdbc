package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.conversion.TypeConversionService;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    DataSource dataSource;
    Connection connection;
    UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = TestDataSourceConfig.create();
        connection = dataSource.getConnection();
        TypeConversionService typeConversionService = new TypeConversionService();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource, typeConversionService);
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var optionalUser = userDao.findById(connection, 1L);

        Assertions.assertThat(optionalUser)
                .get()
                .extracting(User::getAccount)
                .isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var optionalUser = userDao.findByAccount(connection, account);

        Assertions.assertThat(optionalUser)
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var optionalActual = userDao.findById(connection, 2L);

        Assertions.assertThat(optionalActual)
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void update() {
         // given
        final var newPassword = "password99";
        final var user = userDao.findById(connection, 1L).get();
        user.changePassword(newPassword);

        // when
        userDao.update(connection, user);

        // then
        final var optionalActual = userDao.findById(connection, 1L);

        Assertions.assertThat(optionalActual)
                .get()
                .extracting(User::getPassword)
                .isEqualTo(newPassword);
    }
}
