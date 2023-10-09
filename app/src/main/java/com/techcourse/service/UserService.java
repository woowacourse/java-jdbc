package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.support.TransactionExecutor;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionExecutor transactionExecutor;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionExecutor executor) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionExecutor = executor;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(connection -> executeChangePassword(id, newPassword, createBy));
    }

    private void executeChangePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
