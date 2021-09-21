package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.web.annotation.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final RowMapper<User> userRowMapper = rs -> new User(
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
        String sql = "update users set password = ?, email = ? where account = ? ";
        jdbcTemplate.update(sql, user.getPassword(), user.getEmail(), user.getAccount());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return (List<User>) jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.query(sql, userRowMapper, id);
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.query(sql, userRowMapper, account);
    }

}
