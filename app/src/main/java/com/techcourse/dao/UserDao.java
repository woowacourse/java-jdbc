package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.exception.ResultSizeEmptyException;
import nextstep.web.annotation.Repository;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, rowMapper);
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return queryForUser(sql, id);
    }

    public Optional<User> findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return queryForUser(sql, account);
    }

    private Optional<User> queryForUser(String sql, Object value) {
        try {
            return Optional.of(jdbcTemplate.query(sql, rowMapper, value));
        } catch (ResultSizeEmptyException exception) {
            return Optional.empty();
        }
    }

    public void deleteById(Long id) {
        String sql = "delete from users where id = ?";
        jdbcTemplate.delete(sql, id);
    }
}
