package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.PlatformTransactionManager;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserServiceImpl(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
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
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
