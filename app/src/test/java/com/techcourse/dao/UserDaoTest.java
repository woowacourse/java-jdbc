package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UserDaoTest {

    private static final Logger log = LoggerFactory.getLogger(UserDaoTest.class);
    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        } catch (SQLException exception) {
            throw new DataAccessException("테이블 생성에 실패했습니다: ", exception);
        }

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L).orElseThrow();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account).orElseThrow();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).orElseThrow();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        final var affected = userDao.update(user);

        assertThat(affected).isEqualTo(1);

        final var actual = userDao.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
