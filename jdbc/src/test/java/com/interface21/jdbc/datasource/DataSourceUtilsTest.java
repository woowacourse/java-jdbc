package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    @Test
    @DisplayName("일반 연결을 자동 등록하지 않고 요청마다 새로 얻어 반환한다")
    void ordinaryConnectionsAreFreshAndNeverAutomaticallyBound() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var first = mock(Connection.class);
        final var second = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(first, second);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(first);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(first, dataSource);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(second);
        DataSourceUtils.releaseConnection(second, dataSource);

        verify(dataSource, times(2)).getConnection();
        verify(first).close();
        verify(second).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("등록된 연결은 재사용하고 트랜잭션 등록 해제 전까지 닫지 않는다")
    void transactionConnectionIsReusedAndNotReleasedWhileBound() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
            assertThat(DataSourceUtils.doGetConnection(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
            DataSourceUtils.doReleaseConnection(connection, dataSource);
            verifyNoInteractions(dataSource, connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    @DisplayName("트랜잭션 연결이 등록돼 있어도 다른 연결은 정상 종료한다")
    void unrelatedConnectionIsClosedWithoutDisturbingTheBoundOne() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var bound = mock(Connection.class);
        final var unrelated = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, bound);
        try {
            DataSourceUtils.releaseConnection(unrelated, dataSource);
            DataSourceUtils.releaseConnection(null, dataSource);

            verify(unrelated).close();
            verify(bound, never()).close();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(bound);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("연결 획득과 종료 실패의 SQL 원인을 보존하고 비검사 예외로 변환한다")
    void sqlFailuresAreExposedWithoutLosingTheirCauses() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var getFailure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(getFailure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(getFailure);
        assertSame(getFailure, assertThrows(SQLException.class, () -> DataSourceUtils.doGetConnection(dataSource)));

        final var connection = mock(Connection.class);
        final var closeFailure = new SQLException("close failed");
        doThrow(closeFailure).when(connection).close();
        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(closeFailure);
        assertSame(closeFailure, assertThrows(SQLException.class,
                () -> DataSourceUtils.doReleaseConnection(connection, dataSource)));
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
