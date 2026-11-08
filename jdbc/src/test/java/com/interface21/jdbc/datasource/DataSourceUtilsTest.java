package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("바인딩된 커넥션이 없으면 새 커넥션을 얻어 바인딩한다")
    void getNewConnection() throws SQLException {
        final Connection actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        verify(dataSource).getConnection();
    }

    @Test
    @DisplayName("바인딩된 커넥션이 있으면 새로 얻지 않고 그 커넥션을 반환한다")
    void getBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        verify(dataSource, never()).getConnection();
    }

    @Test
    @DisplayName("커넥션을 얻지 못하면 CannotGetJdbcConnectionException으로 감싸 던진다")
    void failToGetConnection() throws SQLException {
        final SQLException cause = new SQLException("connection failed");
        given(dataSource.getConnection()).willThrow(cause);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(cause);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("트랜잭션 밖(autoCommit=true)의 커넥션은 바인딩을 해제하고 닫는다")
    void releaseConnectionOutsideTransaction() throws SQLException {
        given(connection.getAutoCommit()).willReturn(true);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("트랜잭션 중(autoCommit=false)인 커넥션은 닫지 않고 바인딩을 유지한다")
    void releaseConnectionInTransaction() throws SQLException {
        given(connection.getAutoCommit()).willReturn(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("커넥션을 닫지 못하면 원인을 담아 CannotGetJdbcConnectionException을 던진다")
    void failToCloseConnection() throws SQLException {
        final SQLException cause = new SQLException("close failed");
        given(connection.getAutoCommit()).willReturn(true);
        doThrow(cause).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(cause);
    }
}
