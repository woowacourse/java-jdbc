package com.techcourse.service;

import java.sql.Connection;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id, final Connection conn) {
        return TransactionExecutor.executeInReadOnly(conn, () -> userDao.findById(id, conn));
    }

    public void insert(final User user, final Connection conn) {
		TransactionExecutor.executeInTransaction(conn, () -> userDao.insert(user, conn));
    }

    public void changePassword(final long id, final String newPassword, final String createBy, final Connection conn) {
        final var user = findById(id, conn);
        user.changePassword(newPassword);

		TransactionExecutor.executeInTransaction(conn, () -> {
			userDao.update(user, conn);
			userHistoryDao.log(new UserHistory(user, createBy), conn);
		});
	}
}
