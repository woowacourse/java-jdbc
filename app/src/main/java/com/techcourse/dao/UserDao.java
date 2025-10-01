package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final InsertJdbcTemplate insertJdbcTemplate;
    private final UpdateJdbcTemplate updateJdbcTemplate;
    private final SelectJdbcTemplate selectJdbcTemplate;
    private final DeleteAllJdbcTemplate deleteAllJdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.insertJdbcTemplate = new InsertJdbcTemplate(dataSource);
        this.updateJdbcTemplate = new UpdateJdbcTemplate(dataSource);
        this.selectJdbcTemplate = new SelectJdbcTemplate(dataSource);
        this.deleteAllJdbcTemplate = new DeleteAllJdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        insertJdbcTemplate.update(user);
    }

    public void update(final User user) {
        updateJdbcTemplate.update(user);
    }

    public List<User> findAll() {
        return selectJdbcTemplate.findAll();
    }

    public User findById(final Long id) {
        return selectJdbcTemplate.findById(id);
    }

    public User findByAccount(final String account) {
        return selectJdbcTemplate.findByAccount(account);
    }
    
    public void deleteAll() {
        deleteAllJdbcTemplate.update(null);
    }
}
