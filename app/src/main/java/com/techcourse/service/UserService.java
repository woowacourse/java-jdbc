package com.techcourse.service;

import java.sql.Connection;

import javax.sql.DataSource;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
	private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
		this.dataSource = dataSource;
    }

    public User findById(final long id) {
		final Connection conn = DataSourceUtils.getConnection(dataSource);

		try {
        	return TransactionExecutor.executeInReadOnly(conn, () -> userDao.findById(id, conn));
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
			TransactionSynchronizationManager.unbindResource(dataSource);
		}
    }

    public void insert(final User user) {
		final Connection conn = DataSourceUtils.getConnection(dataSource);

		try {
			TransactionExecutor.executeInTransaction(conn, () -> userDao.insert(user, conn));
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
			TransactionSynchronizationManager.unbindResource(dataSource);
		}
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
		final Connection conn = DataSourceUtils.getConnection(dataSource);

		try {
			TransactionExecutor.executeInTransaction(conn, () -> {
				final var user = userDao.findById(id, conn);
				user.changePassword(newPassword);
				userDao.update(user, conn);
				userHistoryDao.log(new UserHistory(user, createBy), conn);
			});
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
			TransactionSynchronizationManager.unbindResource(dataSource);
		}
	}
}
