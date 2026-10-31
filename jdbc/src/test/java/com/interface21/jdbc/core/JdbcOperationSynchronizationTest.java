package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcOperationSynchronizationTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final JdbcCommand command = new JdbcCommand(dataSource);

    @AfterEach
    void cleanUp() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("작업을 위해 생성한 커넥션은 사용 후 닫고 등록을 해제한다")
    void closesAndUnbindsAConnectionCreatedForOneOperation() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("update users")).thenReturn(statement);

        command.execute("update users");

        verify(statement).close();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("이미 등록된 커넥션은 닫거나 해제하지 않고 재사용한다")
    void reusesBoundConnectionWithoutClosingOrUnbindingIt() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(connection.prepareStatement("update users")).thenReturn(statement);

        command.execute("update users");
        command.execute("update users");

        verify(dataSource, never()).getConnection();
        verify(statement, times(2)).close();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("커넥션을 닫는 데 실패해도 등록은 해제한다")
    void unbindsEvenWhenClosingTheConnectionFails() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("update users")).thenReturn(statement);
        var closeFailure = new SQLException("close failed");
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> command.execute("update users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(closeFailure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
