package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionException;

class TransactionManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private TransactionManager transactionManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(DataSourceUtils.getConnection(dataSource)).thenReturn(connection);
        transactionManager = new TransactionManager(dataSource);
    }

    @Nested
    class Execute {
        @Test
        void commit() throws SQLException {
            // given
            Runnable command = mock(Runnable.class);

            // when
            transactionManager.execute(command);

            // then
            verify(command, times(1)).run();
            verify(connection, times(1)).setAutoCommit(false);
            verify(connection, times(1)).commit();
        }

        @Test
        void rollback() throws SQLException {
            // given
            Runnable command = () -> {
                throw new RuntimeException("Test");
            };

            // when
            // then
            assertThatThrownBy(() -> transactionManager.execute(command))
                    .isInstanceOf(DataAccessException.class);

            verify(connection, times(1)).rollback();
        }
    }

    @Nested
    class Query {
        @Test
        void commit() throws SQLException {
            // given
            Supplier<String> query = () -> "result";

            // when
            String result = transactionManager.query(query);

            // then
            verify(connection, times(1)).setReadOnly(true);
            verify(connection, times(1)).setAutoCommit(false);
            verify(connection, times(1)).commit();

            assertThat(result).isEqualTo("result");
        }

        @Test
        void rollback() throws SQLException {
            // given
            Supplier<String> query = () -> {
                throw new RuntimeException("Test");
            };

            // when
            // then
            assertThatThrownBy(() -> transactionManager.query(query))
                    .isInstanceOf(TransactionException.class);

            verify(connection, times(1)).rollback();
        }
    }

    @Test
    void release() throws SQLException {
        // given
        Runnable command = mock(Runnable.class);

        // when
        transactionManager.execute(command);

        // then
        verify(connection, times(1)).setAutoCommit(true);
        verify(connection, times(1)).close();
    }

}
