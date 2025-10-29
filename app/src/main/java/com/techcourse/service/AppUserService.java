package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.service.TransactionService.Transaction;

public class AppUserService {

    private final TransactionService transactionService;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(TransactionService transactionService, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.transactionService = transactionService;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }
    
    public void save(User user) {
        userDao.insert(user);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try(Transaction transaction = transactionService.begin()) {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transaction.commit();
        }
    }
}
