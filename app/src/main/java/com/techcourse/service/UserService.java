package com.techcourse.service;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

@Component
public class UserService {

    @Inject
    private UserDao userDao;

    @Inject
    private UserHistoryDao userHistoryDao;

    private UserService() {}

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public User findByAccount(String account) {
        return userDao.findByAccount(account);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
