package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.util.NoSuchElementException;
import org.springframework.jdbc.core.TransactionManager;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        return transactionManager.execute(connection -> userDao.findById(connection, id)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다.")));
    }

    public void insert(final User user) {
        transactionManager.executeNoReturn(connection -> userDao.insert(connection, user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.executeNoReturn(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
