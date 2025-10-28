package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    @Override
    public void save(final User user) {
        if (userDao.findByAccount(user.getAccount()) == null) {
            userDao.insert(user);
            return;
        }
        userDao.update(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createdBy));
    }
}
