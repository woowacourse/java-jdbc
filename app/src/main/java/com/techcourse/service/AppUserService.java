package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import java.util.Objects;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
        this.userHistoryDao = Objects.requireNonNull(userHistoryDao, "userHistoryDao must not be null");
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id);
    }

    @Override
    public void save(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final var user = findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createdBy));
    }
}
