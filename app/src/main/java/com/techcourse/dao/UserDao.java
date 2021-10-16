package com.techcourse.dao;

import com.techcourse.domain.User;
import di.annotation.Repository;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.mapper.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static UserDao instance;

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper<User> userMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return Optional.ofNullable(jdbcTemplate.executeForObject(sql, userMapper, id));
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return Optional.ofNullable(jdbcTemplate.executeForObject(sql, userMapper, account));
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.executeForList(sql, userMapper);
    }

    public int update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        return jdbcTemplate.execute(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public int deleteById(long id) {
        final String sql = "delete from users where id = ?";
        return jdbcTemplate.execute(sql, id);
    }
}
