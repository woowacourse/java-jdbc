package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    @Test
    @DisplayName("트랜잭션 밖에서 연 연결은 등록 없이 사용하고 즉시 반납한다")
    void releasesConnectionOutsideTransaction() throws SQLException {
        // given
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        // when
        final var acquired = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(acquired, dataSource);

        // then
        assertThat(acquired).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    @DisplayName("트랜잭션에 등록된 연결은 재사용하고 등록을 해제할 때까지 열어 둔다")
    void retainsTransactionConnectionUntilUnbound() throws SQLException {
        // given
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            // when
            final var acquired = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(acquired, dataSource);

            // then
            assertThat(acquired).isSameAs(connection);
            verifyNoInteractions(dataSource);
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        verify(connection).close();
    }

    @Test
    @DisplayName("연결 반납이 실패하면 원래 SQL 예외를 전달한다")
    void preservesReleaseFailureCause() throws SQLException {
        // given
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var closeFailure = new SQLException("연결 종료 실패");
        doThrow(closeFailure).when(connection).close();

        // when
        final var thrown = assertThrows(CannotGetJdbcConnectionException.class,
                () -> DataSourceUtils.releaseConnection(connection, dataSource));

        // then
        assertThat(thrown).hasCause(closeFailure);
    }
}
