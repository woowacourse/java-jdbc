package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;

public class MockUserDao extends UserDao {

    public MockUserDao(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void insert(User user) {
        throw new DataAccessException();
    }
}
