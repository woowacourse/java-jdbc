package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.exception.UserUpdateFailureException;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.exception.ResultSizeEmptyException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Repository;

import javax.sql.DataSource;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    @Autowired
    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public User insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Long id = jdbcTemplate.insert(sql, Long.class, user.getAccount(), user.getPassword(), user.getEmail());
        return new User(id, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        int updateCount = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        if (updateCount != 1) {
            throw new UserUpdateFailureException(user);
        }
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
