package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

  private final DataSource dataSource = mock(DataSource.class);
  private final Connection connection = mock(Connection.class);

  @AfterEach
  void tearDown() {
    TransactionSynchronizationManager.unbindResource(dataSource);
  }

  @Test
  void standaloneCallsObtainNewConnectionsWithoutBinding() throws SQLException {
    final Connection otherConnection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection, otherConnection);

    assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
    DataSourceUtils.releaseConnection(connection, dataSource);
    assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(otherConnection);
    DataSourceUtils.releaseConnection(otherConnection, dataSource);

    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    verify(dataSource, times(2)).getConnection();
    verify(connection).close();
    verify(otherConnection).close();
  }

  @Test
  void boundConnectionIsReusedAndKeptOpenUntilUnbound() throws SQLException {
    TransactionSynchronizationManager.bindResource(dataSource, connection);

    assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
    DataSourceUtils.releaseConnection(connection, dataSource);

    verify(dataSource, never()).getConnection();
    verify(connection, never()).close();
    TransactionSynchronizationManager.unbindResource(dataSource);
    DataSourceUtils.releaseConnection(connection, dataSource);
    verify(connection).close();
  }

  @Test
  void acquisitionFailurePreservesCauseAsDataAccessException() throws SQLException {
    final SQLException cause = new SQLException("Connection acquisition failed");
    when(dataSource.getConnection()).thenThrow(cause);

    assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
        .isInstanceOf(DataAccessException.class)
        .hasCause(cause);
  }

  @Test
  void closingFailurePreservesCauseAsDataAccessException() throws SQLException {
    final SQLException cause = new SQLException("Connection close failed");
    doThrow(cause).when(connection).close();

    assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
        .isInstanceOf(DataAccessException.class)
        .hasCause(cause);
  }
}
