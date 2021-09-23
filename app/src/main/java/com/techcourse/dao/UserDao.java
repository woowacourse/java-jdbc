package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.mapper.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final ObjectMapper<User> userMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.executeForObject(sql, userMapper, id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.executeForObject(sql, userMapper, account);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.executeForList(sql, userMapper);
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final int affectedCount = jdbcTemplate.execute(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
        if (affectedCount != 1) {
            throw new DaoMethodExecutionFailureException("유저 정보 업데이트에 실패했습니다.");
        }
    }

    public int deleteById(long id) {
        final String sql = "delete from users where id = ?";
        return jdbcTemplate.execute(sql, id);
    }
}
