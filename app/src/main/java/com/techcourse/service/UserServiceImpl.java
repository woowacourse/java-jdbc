package com.techcourse.service;

import com.interface21.jdbc.transaction.TransactionProxy;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserServiceImpl implements UserService {

    private static final UserService INSTANCE = createWithTransaction(DataSourceConfig.getInstance());

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    private UserServiceImpl(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public static UserService createWithTransaction(DataSource dataSource) {
        UserDao userDao = new UserDao(dataSource);
        UserHistoryDao userHistoryDao = new UserHistoryDao(dataSource);
        return createWithTransaction(userDao, userHistoryDao);
    }

    public static UserService createWithTransaction(UserDao userDao, UserHistoryDao userHistoryDao) {
        UserService userService = new UserServiceImpl(userDao, userHistoryDao);
        return TransactionProxy.createProxy(userService, UserService.class, DataSourceConfig.getInstance());
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
