package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {
	private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

	private final AppUserService appUserService;
	private final DataSource dataSource = DataSourceConfig.getInstance();

	public TxUserService(final AppUserService appUserService) {
		this.appUserService = appUserService;
	}

	public User findById(final long id) {
		return appUserService.findById(id);
	}

	public void insert(final User user) {
		appUserService.insert(user);
	}

	public void changePassword(final long id, final String newPassword, final String createBy) {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			connection.setAutoCommit(false);
			appUserService.changePassword(id, newPassword, createBy);
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException ignored) {}
			throw new DataAccessException("Failed to change password", e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
			TransactionSynchronizationManager.unbindResource(dataSource);
		}
	}
}
