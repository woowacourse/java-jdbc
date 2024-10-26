package com.interface21.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

	private static final TransactionManager INSTANCE = new TransactionManager();
	private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

	private TransactionManager() {
	}

	public static TransactionManager getInstance() {
		return INSTANCE;
	}

	public void beginTransaction(final DataSource dataSource, final Consumer<Connection> consumer) {
		final Connection connection = DataSourceUtils.getConnection(dataSource);
		setAutoCommit(connection, false);

		try {
			consumer.accept(connection);
			commit(connection);
		} catch (Exception e) {
			rollback(connection);
			log.info("로직 처리 중 에러가 발생했습니다. 롤백합니다.");
			throw new DataAccessException("로직 처리 중 에러가 발생했습니다. 롤백합니다.", e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	private void setAutoCommit(final Connection connection, final boolean autoCommit) {
		try {
			connection.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			throw new DataAccessException("데이터베이스 커넥션의 AutoCommit 설정 중 에러가 발생했습니다.", e);
		}
	}

	private void commit(final Connection connection) {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new DataAccessException("커넥션 commit 중 에러가 발생했습니다.", e);
		}
	}

	private void rollback(final Connection connection) {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new DataAccessException("커넥션 rollback 중 에러가 발생했습니다.", e);
		}
	}
}
