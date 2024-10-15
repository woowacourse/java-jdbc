package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

    private final UserHistoryDao userHistoryDao;
    private final UserDao userDao;

    public AppUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(final Long id) {
        return userDao.findById(id);
    }

    @Override
    public void save(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final Long userId, final String newPassword, final String createdBy) {
        final User user = findById(userId);
        user.changePassword(newPassword);
        userDao.update(user);

        final UserHistory userHistory = new UserHistory(user, createdBy);
        userHistoryDao.log(userHistory);
    }
}
