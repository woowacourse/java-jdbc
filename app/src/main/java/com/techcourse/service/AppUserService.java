package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public void insert(User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        User user = getById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createdBy));
    }

    @Override
    public User getById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find user with id: " + id));
    }
}
