package com.techcourse.dao;

import com.techcourse.dao.statement.InsertPreparedStatement;
import com.techcourse.dao.statement.SelectAllResultSet;
import com.techcourse.dao.statement.SelectByAccountResultSet;
import com.techcourse.dao.statement.SelectByIdResultSet;
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
        jdbcTemplate.executeQuery(new InsertPreparedStatement(user));
    }

    public void update(final User user) {
        jdbcTemplate.executeQuery(new UpdatePreparedStatement(user));
    }

    public List<User> findAll() {
        return (List<User>) jdbcTemplate.executeQueryForList(new SelectAllResultSet());
    }

    public User findById(final Long id) {
        return (User) jdbcTemplate.executeQueryForObject(new SelectByIdResultSet(), new Object[]{id});
    }

    public User findByAccount(final String account) {
        return (User) jdbcTemplate.executeQueryForObject(new SelectByAccountResultSet(), new Object[]{account});
    }
}
