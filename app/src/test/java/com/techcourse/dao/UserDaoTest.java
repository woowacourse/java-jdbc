package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.interface21.dao.NoResultFoundException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.TransactionManager;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserDaoTest {

    private UserDao userDao;
    private TransactionManager transactionManager = new TransactionManager(DataSourceConfig.getInstance());

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");

        try {
            transactionManager.executeInTransaction(() ->
                    userDao.findByAccount("gugu")
            );
        } catch (NoResultFoundException e) {
            transactionManager.executeInTransaction(() ->
                    userDao.insert(user)
            );
        }
    }

    @Test
    void findAll() {
        final var users = transactionManager.getResultInTransaction(() ->
                userDao.findAll()
        );
        users.forEach(System.out::println);
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = transactionManager.getResultInTransaction(() ->
                userDao.findById(1L)
        );

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = transactionManager.getResultInTransaction(() ->
                userDao.findByAccount(account)
        );

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        transactionManager.executeInTransaction(() ->
                userDao.insert(user)
        );

        final var actual = transactionManager.getResultInTransaction(() ->
                userDao.findById(2L)
        );

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = transactionManager.getResultInTransaction(() ->
                userDao.findById(1L)
        );
        user.changePassword(newPassword);

        transactionManager.executeInTransaction(() ->
                userDao.update(user)
        );

        final var actual = transactionManager.getResultInTransaction(() ->
                userDao.findById(1L)
        );

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
