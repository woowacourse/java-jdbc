package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.config.TransactionManagerConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserDaoTest {

    private UserDao userDao;
    private DataSource dataSource = DataSourceConfig.getInstance();

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(new JdbcTemplate(dataSource));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            userDao.insert(user);
        });
    }

    @Test
    void findAll() {
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            final var users = userDao.findAll();
            assertThat(users).isNotEmpty();
        });
    }

    @Test
    void findById() {
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            final var user = userDao.findById(1L);

            assertThat(user.getAccount()).isEqualTo("gugu");
        });
    }

    @Test
    void findByAccount() {
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            final var account = "gugu";
            final var user = userDao.findByAccount(account);

            assertThat(user.getAccount()).isEqualTo(account);
        });
    }

    @Test
    void insert() {
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            final var account = "insert-gugu";
            final var user = new User(account, "password", "hkkang@woowahan.com");
            userDao.insert(user);

            final var actual = userDao.findById(2L);

            assertThat(actual.getAccount()).isEqualTo(account);
        });
    }

    @Test
    void update() {
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            final var newPassword = "password99";
            final var user = userDao.findById(1L);
            user.changePassword(newPassword);

            userDao.update(user);

            final var actual = userDao.findById(1L);

            assertThat(actual.getPassword()).isEqualTo(newPassword);
        });
    }

    @Test
    void delete() {
        TransactionManagerConfig.executeInTransaction(dataSource, conn -> {
            final var user = userDao.findById(1L);
            userDao.delete(user);

            final var actual = userDao.findById(1L);

            assertThat(actual).isNull();
        });
    }
}
