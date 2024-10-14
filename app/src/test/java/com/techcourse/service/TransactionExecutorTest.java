package com.techcourse.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.dao.DataAccessException;

class TransactionExecutorTest {

	private Connection conn;

	@BeforeEach
	void setUp() {
		conn = mock(Connection.class);
	}

	@DisplayName("트랜잭션 내에서 실행한다.")
	@Test
	void testExecuteInTransaction() throws SQLException {
		Runnable runnable = () -> {
		};

		TransactionExecutor.executeInTransaction(conn, runnable);

		verify(conn).setAutoCommit(false);
		verify(conn).commit();
	}

	@DisplayName("트랜잭션 내에서 실행 중 예외가 발생하면 롤백한다.")
	@Test
	void testExecuteInTransaction_RollbackOnException() throws SQLException {
		Runnable runnable = () -> {
			throw new RuntimeException("예외");
		};

		assertThatThrownBy(() -> TransactionExecutor.executeInTransaction(conn, runnable))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("예외");

		verify(conn).setAutoCommit(false);
		verify(conn).rollback();
	}

	@DisplayName("읽기 전용으로 실행한다.")
	@Test
	void testExecuteInReadOnly_Success() throws Exception {
		Callable<String> callable = () -> "성공";

		String result = TransactionExecutor.executeInReadOnly(conn, callable);

		assertThat(result).isEqualTo("성공");
		verify(conn).setReadOnly(true);
	}

	@DisplayName("읽기 전용으로 실행 중 예외가 발생하면 DataAccessException을 던진다.")
	@Test
	void testExecuteInReadOnly_Exception() throws Exception {
		Callable<String> callable = () -> {
			throw new RuntimeException("예외");
		};

		assertThatThrownBy(() -> TransactionExecutor.executeInReadOnly(conn, callable))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("예외");

		verify(conn).setReadOnly(true);
	}
}
