package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.interface21.transaction.support.TransactionSynchronizationManager;

class DataSourceUtilsTest {

    @Nested
    @DisplayName("connection 조회")
    class GetConnection {

        @Test
        @DisplayName("성공 : TransactionSynchronizationManager에 값이 있을 경우")
        void getConnectionWithExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection connection = Mockito.mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            Connection actual = DataSourceUtils.getConnection(dataSource);

            assertThat(actual).isEqualTo(connection);
        }

        @Test
        @DisplayName("성공 : TransactionSynchronizationManager에 값이 없을 경우")
        void getConnectionWithNotExists() throws SQLException {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection connection = Mockito.mock(Connection.class);
            when(dataSource.getConnection()).thenReturn(connection);

            Connection actual = DataSourceUtils.getConnection(dataSource);

            assertThat(actual).isEqualTo(connection);
        }
    }

    @Nested
    @DisplayName("connection 종료")
    class ReleaseConnection {

        @Test
        @DisplayName("성공 : TransactionSynchronizationManager에 값이 있을 경우 (주입하는 커넥션과 manager에 있는 커넥션 동일)")
        void releaseConnectionWithExistsSameConnection() {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection connection = Mockito.mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            DataSourceUtils.releaseConnection(connection, dataSource);

            assertAll(
                    () -> verify(connection).close(),
                    () -> assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull()
            );
        }

        @Test
        @DisplayName("성공 : TransactionSynchronizationManager에 값이 있을 경우 (주입하는 커넥션과 manager에 있는 커넥션 다름)")
        void releaseConnectionWithExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection firstConnection = Mockito.mock(Connection.class);
            Connection secondConnection = Mockito.mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, firstConnection);

            DataSourceUtils.releaseConnection(secondConnection, dataSource);

            assertAll(
                    () -> verify(firstConnection).close(),
                    () -> verify(secondConnection).close(),
                    () -> assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull()
            );
        }

        @Test
        @DisplayName("성공 : TransactionSynchronizationManager에 값이 없을 경우")
        void releaseConnectionWithNotExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection connection = Mockito.mock(Connection.class);

            DataSourceUtils.releaseConnection(connection, dataSource);

            assertAll(
                    () -> verify(connection).close(),
                    () -> assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull()
            );
        }
    }
}
