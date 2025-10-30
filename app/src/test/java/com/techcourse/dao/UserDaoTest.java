package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.conversion.TypeConversionService;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    UserDao userDao;
    DataSource dataSource;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = TestDataSourceConfig.create();
        TypeConversionService typeConversionService = new TypeConversionService();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource, typeConversionService);
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);
    }

    @Test
    void findAll() throws SQLException {
        final var users = userDao.findAll(dataSource.getConnection());

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        final var optionalUser = userDao.findById(dataSource.getConnection(), 1L);

        Assertions.assertThat(optionalUser)
                .get()
                .extracting(User::getAccount)
                .isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        final var account = "gugu";
        final var optionalUser = userDao.findByAccount(dataSource.getConnection(), account);

        Assertions.assertThat(optionalUser)
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);

        final var optionalActual = userDao.findById(dataSource.getConnection(), 2L);

        Assertions.assertThat(optionalActual)
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
         // given
        final var newPassword = "password99";
        final var user = userDao.findById(dataSource.getConnection(), 1L).get();
        user.changePassword(newPassword);

        // when
        userDao.update(dataSource.getConnection(), user);

        // then
        final var optionalActual = userDao.findById(dataSource.getConnection(), 1L);

        Assertions.assertThat(optionalActual)
                .get()
                .extracting(User::getPassword)
                .isEqualTo(newPassword);
    }
}
