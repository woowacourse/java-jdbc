package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(final UserDao userDao,
                       final UserHistoryDao userHistoryDao,
                       final TransactionTemplate transactionTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = transactionTemplate;
    }

    public User getById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 user를 찾을 수 없습니다, id: " + id));
    };

    public void insert(final User user) {
        transactionTemplate.execute(connection ->
                userDao.insert(connection, user)
        );
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(connection -> {
            final var user = getByIdInTransaction(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private User getByIdInTransaction(final Connection connection, final long id) {
        return userDao.findById(connection, id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 user를 찾을 수 없습니다, id: " + id));
    }
}
