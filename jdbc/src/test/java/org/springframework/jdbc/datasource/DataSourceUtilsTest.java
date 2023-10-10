package org.springframework.jdbc.datasource;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DataSourceUtilsTest {

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;

    @Test
    @DisplayName("트랜잭션이 아닌 경우, Connection이 닫힌다.")
    void closeConnectionIfNotInTransaction() throws SQLException {
        //given
        when(dataSource.getConnection()).thenReturn(connection);

        //when
        DataSourceUtils.closeConnectionIfNotInTransaction(dataSource, connection);

        //then
        verify(connection).close();
    }

    @Test
    @DisplayName("트랜잭션이 아닌 경우, Connection이 닫힌다.")
    void notCloseConnectionIfInTransaction() throws SQLException {
        //given
        when(dataSource.getConnection()).thenReturn(connection);
        Connection transactionConnection = DataSourceUtils.getConnection(dataSource);

        //when
        DataSourceUtils.closeConnectionIfNotInTransaction(dataSource, transactionConnection);

        //then
        verify(connection, never()).close();
    }

}
