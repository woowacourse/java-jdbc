package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        userDao = new UserDao(jdbcTemplate);
        jdbcTemplate.update("truncate table users restart identity");
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
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("트랜잭션을 커밋하면 데이터가 반영된다.")
    @Test
    void updateWithTransactionCommit() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);

        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);
        userDao.update(user);

        conn.commit();
        DataSourceUtils.releaseConnection(conn, dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("트랜잭션을 롤백하면 데이터가 반영되지 않는다.")
    @Test
    void updateWithTransactionRollback() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);

        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);
        userDao.update(user);

        conn.rollback();
        DataSourceUtils.releaseConnection(conn, dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
