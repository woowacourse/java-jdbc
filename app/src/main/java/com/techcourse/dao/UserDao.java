package com.techcourse.dao;

import com.techcourse.dao.statement.InsertPreparedStatement;
import com.techcourse.dao.statement.SelectAllResultSet;
import com.techcourse.dao.statement.SelectResultSet;
import com.techcourse.dao.statement.UpdatePreparedStatement;
import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeQuery(new InsertPreparedStatement(user), sql);
    }

    public void update(final User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.executeQuery(new UpdatePreparedStatement(user), sql);
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return (List<User>) jdbcTemplate.executeQueryForList(new SelectAllResultSet(), sql);
    }

    public User findById(final Long id) {
        String sql = "select * from users where id = ?";
        return (User) jdbcTemplate.executeQueryForObject(new SelectResultSet(), sql, new Object[]{id});
    }

    public User findByAccount(final String account) {
        String sql = "select * from users where account = ?";
        return (User) jdbcTemplate.executeQueryForObject(new SelectResultSet(), sql, new Object[]{account});
    }
}
