package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    private User gugu;
    private User josh;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
        gugu = new User("gugu", "password", "hkkang@woowahan.com");
        josh = new User("josh", "password", "whgusrms96@gmail.com");

        try (final Connection conn = DataSourceConfig.getInstance().getConnection()) {
            userDao.insert(conn, gugu);
            userDao.insert(conn, josh);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void refresh() throws SQLException {
        final String sql = "DELETE FROM users";
        jdbcTemplate.update(DataSourceConfig.getInstance().getConnection(), sql);
    }

    @Test
    void findAll() throws SQLException {
        final List<User> users = userDao.findAll(DataSourceConfig.getInstance().getConnection());

        assertAll(
                () -> assertThat(users.size()).isEqualTo(2),
                () -> assertThat(users).contains(gugu),
                () -> assertThat(users).contains(josh)
        );
    }

    @Test
    void findById() throws SQLException {
        final User user = userDao.findById(DataSourceConfig.getInstance().getConnection(), gugu.getId());

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        final String account = "gugu";
        final User user = userDao.findByAccount(DataSourceConfig.getInstance().getConnection(), account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");

        try (Connection conn = DataSourceConfig.getInstance().getConnection()) {
            userDao.insert(conn, user);

            final User actual = userDao.findById(conn, user.getId());

            assertThat(actual.getAccount()).isEqualTo(account);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    void update() {
        final String newPassword = "password99";

        try (Connection conn = DataSourceConfig.getInstance().getConnection()) {
            final User user = userDao.findById(conn, gugu.getId());
            user.changePassword(newPassword);

            userDao.update(conn, user);

            final User actual = userDao.findById(conn, gugu.getId());
            assertThat(actual.getPassword()).isEqualTo(newPassword);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
