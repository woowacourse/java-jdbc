package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("동기화 여부에 따른 JDBC 연결 획득과 해제")
class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("트랜잭션 밖에서는 매번 새 연결을 얻고 스레드에 보관하지 않는다")
    void obtainsNewConnectionsWithoutBindingOutsideTransaction() throws SQLException {
        final var second = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection, second);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(second);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(dataSource, times(2)).getConnection();
    }

    @Test
    @DisplayName("스레드에 연결된 Connection이 있으면 DataSource를 호출하지 않고 재사용한다")
    void reusesBoundConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource);
    }

    @Test
    @DisplayName("연결 획득 실패를 원인을 보존한 데이터 접근 예외로 변환한다")
    void preservesConnectionAcquisitionFailure() throws SQLException {
        final var failure = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(failure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("트랜잭션에 연결되지 않은 Connection은 즉시 닫는다")
    void closesUnboundConnection() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    @DisplayName("트랜잭션에 연결된 Connection은 DAO 호출이 끝나도 닫지 않는다")
    void keepsBoundConnectionOpen() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verifyNoInteractions(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("해제할 Connection이 없으면 아무 작업도 하지 않는다")
    void acceptsNullConnection() {
        DataSourceUtils.releaseConnection(null, dataSource);

        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("연결 종료 실패를 원인을 보존한 데이터 접근 예외로 변환한다")
    void preservesConnectionClosingFailure() throws SQLException {
        final var failure = new SQLException("종료 실패");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(failure);
    }
}
