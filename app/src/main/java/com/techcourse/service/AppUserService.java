package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.util.Optional;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public Optional<User> findById(final long id) {
        return userDao.findById(id);
    }

    @Override
    public void insert(User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, String newPassword, String createBy) {
        User user = findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.changePassword(newPassword);
        if (userDao.update(user) == 0) {
            throw new UserNotFoundException(id);
        }
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
