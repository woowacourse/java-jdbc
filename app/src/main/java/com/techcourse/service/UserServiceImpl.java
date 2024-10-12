package com.techcourse.service;

import com.interface21.jdbc.transaction.TransactionProxy;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.config.JdbcTemplateConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserServiceImpl implements UserService {

    private static final UserService INSTANCE = TransactionProxy.createProxy(
            new UserServiceImpl(
                    new UserDao(JdbcTemplateConfig.getInstance()),
                    new UserHistoryDao(JdbcTemplateConfig.getInstance())
            ),
            UserService.class,
            DataSourceConfig.getInstance()
    );

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserServiceImpl(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public static UserService getInstance() {
        return INSTANCE;
    }

    public User findById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    public User findByAccount(String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
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
