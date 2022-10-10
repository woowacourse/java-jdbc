package com.techcourse.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;

public class TxUserService implements UserService {

	private final UserService userService;
	private final PlatformTransactionManager transactionManager;

	public TxUserService(UserService userService,
		PlatformTransactionManager transactionManager) {
		this.userService = userService;
		this.transactionManager = transactionManager;
	}

	@Override
	public User findById(final long id) {
		return userService.findById(id);
	}

	@Override
	public void insert(final User user) {
		userService.insert(user);
	}

	@Override
	public void changePassword(final long id, final String newPassword, final String createBy) {
		final TransactionStatus transactionStatus = transactionManager.getTransaction(
			new DefaultTransactionDefinition());

		try {
			userService.changePassword(id, newPassword, createBy);
			transactionManager.commit(transactionStatus);
		} catch (final DataAccessException e) {
			transactionManager.rollback(transactionStatus);
			throw new DataAccessException();
		}
	}
}
