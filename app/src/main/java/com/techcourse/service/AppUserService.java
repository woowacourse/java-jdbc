package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryService userHistoryService;

    public AppUserService(UserDao userDao, UserHistoryService userHistoryService) {
        this.userDao = userDao;
        this.userHistoryService = userHistoryService;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id);
    }

    @Override
    public void insert(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryService.insert(new UserHistory(user, createBy));
    }
}
