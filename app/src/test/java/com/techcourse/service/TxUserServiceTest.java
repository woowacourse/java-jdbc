package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TxUserServiceTest {

  private final DataSource dataSource = mock(DataSource.class);
  private final Connection connection = mock(Connection.class);
  private final UserService appUserService = mock(UserService.class);
  private final UserService userService = new TxUserService(dataSource, appUserService);

  @BeforeEach
  void setUp() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
  }

  @AfterEach
  void tearDown() {
    TransactionSynchronizationManager.unbindResource(dataSource);
  }

  @Test
  void closingFailureDoesNotReplaceOriginalExceptionOrLeaveBinding() throws SQLException {
    final IllegalArgumentException originalException = new IllegalArgumentException("Work failed");
    final SQLException closeException = new SQLException("Connection close failed");
    doThrow(originalException).when(appUserService).changePassword(1L, "new-password", "gugu");
    doThrow(closeException).when(connection).close();

    final IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
        () -> userService.changePassword(1L, "new-password", "gugu"));

    assertThat(actual).isSameAs(originalException);
    assertThat(actual.getSuppressed()).hasSize(1);
    assertThat(actual.getSuppressed()[0]).hasCause(closeException);
    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    verify(connection).rollback();
    verify(connection, never()).commit();
  }

  @Test
  void commitFailureRollsBackAndReleasesConnection() throws SQLException {
    final SQLException cause = new SQLException("Commit failed");
    doThrow(cause).when(connection).commit();

    assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "gugu"))
        .isInstanceOf(DataAccessException.class)
        .hasCause(cause);

    verify(connection).rollback();
    verify(connection).close();
    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
  }

  @Test
  void errorAlsoRollsBackAndReleasesConnection() throws SQLException {
    final AssertionError originalException = new AssertionError("Work failed");
    doThrow(originalException).when(appUserService).changePassword(1L, "new-password", "gugu");

    final AssertionError actual = assertThrows(AssertionError.class,
        () -> userService.changePassword(1L, "new-password", "gugu"));

    assertThat(actual).isSameAs(originalException);
    verify(connection).rollback();
    verify(connection).close();
    verify(connection, never()).setAutoCommit(true);
    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
  }

  @Test
  void existingTransactionIsRejectedWithoutTouchingItsConnection() {
    TransactionSynchronizationManager.bindResource(dataSource, connection);

    assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "gugu"))
        .isInstanceOf(IllegalStateException.class);

    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    verifyNoInteractions(dataSource, connection, appUserService);
  }
}
