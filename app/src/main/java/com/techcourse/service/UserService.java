package com.techcourse.service;

import com.techcourse.dao.SimpleUserDao;
import com.techcourse.dao.SimpleUserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final SimpleUserDao userDao;
    private final SimpleUserHistoryDao userHistoryDao;

    public UserService(final SimpleUserDao userDao, final SimpleUserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
