package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class MockUserDao extends UserDao {

    public MockUserDao(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void update(User user) {
        throw new DataAccessException();
    }
}
