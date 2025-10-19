package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("멤버가 존재하지 않습니다."));
    }

    @Override
    public void save(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        User user = findUser(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createdBy));
    }

    private User findUser(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("멤버가 존재하지 않습니다."));
    }
}
