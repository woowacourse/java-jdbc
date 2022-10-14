package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import nextstep.jdbc.core.JdbcTemplate;
import nextstep.jdbc.exception.DataAccessException;

public class MockUserDao extends UserDao {

    public MockUserDao(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void insert(final User user) {
        throw new DataAccessException();
    }
}
