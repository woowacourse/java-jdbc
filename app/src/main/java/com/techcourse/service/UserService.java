package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.JdbcTemplate;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.userDao = new UserDao(jdbcTemplate);
        this.userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    public void edit(final User user, final String createBy) {
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
