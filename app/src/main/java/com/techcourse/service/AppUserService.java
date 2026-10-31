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
    public User findById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다. id=" + id));
    }

    @Override
    public void insert(User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        final var user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다. id=" + id));
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
