package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.interface21.dao.DataAccessException;

public class TransactionExecutor {

	public static void executeInTransaction(final Connection conn, final Runnable runnable) {
		try {
			conn.setAutoCommit(false);
			runnable.run();
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException rollbackException) {
				throw new DataAccessException(rollbackException);
			}
			throw new DataAccessException(e);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new DataAccessException(e);
			}
		}
	}

	public static <T> T executeInReadOnly(final Connection conn, final Callable<T> callable) {
		try {
			conn.setReadOnly(true);
			return callable.call();
		} catch (Exception e) {
			throw new DataAccessException(e);
		} finally {
			try {
				conn.setReadOnly(false);
			} catch (SQLException e) {
				throw new DataAccessException(e);
			}
		}
	}
}
