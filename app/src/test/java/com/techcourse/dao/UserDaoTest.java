package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate template;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        template = new JdbcTemplate(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        final List<User> users = template.query(sql, userRowMapper());

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        final User user = template.queryForObject(sql, userRowMapper(), 1L).orElseThrow();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        final User user = template.queryForObject(sql, userRowMapper(), account).orElseThrow();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        final String sql1 = "INSERT INTO users(account, password, email) VALUES(?, ?, ?)";
        final int updatedRow = template.update(sql1, user.getAccount(), user.getPassword(), user.getEmail());

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        final String sql2 = "UPDATE users SET password = ? WHERE id = ?";
        template.update(sql2, user.getPassword(), user.getId());
        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    private RowMapper<User> userRowMapper() {
        return rs -> new User(
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
