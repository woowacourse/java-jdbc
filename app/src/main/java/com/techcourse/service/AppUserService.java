package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserServiceInterface {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));
    }

    @Override
    public void save(User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        final var user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createdBy));
    }
}
