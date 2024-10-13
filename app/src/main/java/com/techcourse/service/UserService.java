package com.techcourse.service;

import javax.sql.DataSource;
import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final DataSource datasource;
    private final TransactionTemplate txTemplate;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(
            DataSource datasource,
            UserDao userDao,
            UserHistoryDao userHistoryDao
    ) {
        this.datasource = datasource;
        this.txTemplate = new TransactionTemplate(datasource);
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        txTemplate.executeWithoutResult(() -> {
            User user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);

            UserHistory userHistory = new UserHistory(user, createBy);
            userHistoryDao.log(userHistory);
        });
    }
}
