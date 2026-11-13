package com.interface21.transaction.support;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionSynchronizationManagerTest {
    @Test
    void sharesConnectionUntilTransactionOwnerUnbindsIt() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
            new JdbcTemplate(dataSource).update("update users set password = 'changed'");
            verify(statement).close();
            verify(connection, never()).close();
            verifyNoInteractions(dataSource);
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void isolatesResourcesByThreadAndDataSource() throws Exception {
        final var first = mock(DataSource.class);
        final var second = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var other = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(first, connection);
        TransactionSynchronizationManager.bindResource(second, other);
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
                TransactionSynchronizationManager.bindResource(first, other);
                assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(other);
            }).get();
            assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(other);
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
        }
    }

    @Test
    void ordinaryAccessDoesNotBindConnection() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }
}
