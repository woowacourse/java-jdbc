package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
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
        transactionExecutor.execute(connection -> executeChangePassword(id, newPassword, createBy, connection));
    }

    private void executeChangePassword(final long id, final String newPassword, final String createBy, final Connection connection) {
        final var user = findById(id);
        user.changePassword(newPassword);
        // TODO: 2023-10-05 DAO가 Connection을 몰라도 되도록 수정하기
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }
}
