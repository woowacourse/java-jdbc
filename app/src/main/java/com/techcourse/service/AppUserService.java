package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.service.model.UserService;

import java.util.Optional;

public class AppUserService implements UserService {

    private static final String USER_ID_NOT_EXIST_ERROR_MESSAGE = "%s id 의 유저를 찾지 못했습니다.";

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_ID_NOT_EXIST_ERROR_MESSAGE, id)));
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return userDao.findByAccount(account);
    }

    @Override
    public void save(User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.updateWithTransaction(user);
        userHistoryDao.insertWithTransaction(new UserHistory(user, createBy));
    }
}
