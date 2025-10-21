package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.service.TransactionService.Transaction;
import java.sql.Connection;

public class UserService {

    private final TransactionService transactionService;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(TransactionService transactionService, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.transactionService = transactionService;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try(Transaction begin = transactionService.begin()) {
            return userDao.findById(begin.getConnection(), id);
        }
    }

    public void insert(final User user) {
        try(Transaction transaction = transactionService.begin()) {
            userDao.insert(transaction.getConnection(), user);
            transaction.commit();
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try(Transaction transaction = transactionService.begin()) {
            final var user = findById(id);
            user.changePassword(newPassword);
            Connection connection = transaction.getConnection();
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            transaction.commit();
        }
    }
}
