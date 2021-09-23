package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public int update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";

        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        return jdbcTemplate.queryForList(sql, getUserRowMapper());

    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), id);
    }

    public User findByAccount(String account) {
        final String sql = "select * from users where account = ?";

        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), account);
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs, rm) ->
                new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
    }
}
