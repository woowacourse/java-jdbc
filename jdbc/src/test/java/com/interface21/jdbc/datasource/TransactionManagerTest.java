package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TestService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    private TransactionManager transactionManager;
    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        transactionManager = new TransactionManager(mockDataSource);
        doReturn(mockConnection).when(mockDataSource).getConnection();
    }

    @DisplayName("depth가 2인 transaction의 경우 commit되지 않고 기존 transaction이 전파된다")
    @Test
    void transactionPropagationIsRequired() {
        TestService testService = new TestService(transactionManager);

        testService.depthTwoValidMethod();

        assertAll(
                () -> verify(mockConnection, times(1)).commit(),
                () -> verify(mockConnection, never()).rollback()
        );
    }

    @DisplayName("오류가 발생하지 않는 Supplier의 경우 commit된다")
    @Test
    void transactionSupplierSuccess() throws SQLException {
        Supplier<String> validSupplier = () -> "success";

        String actual = transactionManager.transaction(validSupplier);

        assertAll(
                () -> assertThat(actual).isEqualTo("success"),
                () -> verify(mockConnection, times(1)).commit(),
                () -> verify(mockConnection, never()).rollback()
        );
    }

    @DisplayName("오류가 발생하는 Supplier의 경우 rollback된다")
    @Test
    void transactionSupplierFail() {
        Supplier<Void> exceptionFunction = () -> {
            throw new RuntimeException("exception");
        };

        assertAll(
                () -> assertThatThrownBy(() -> transactionManager.transaction(exceptionFunction))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessage("exception"),
                () -> verify(mockConnection, never()).commit(),
                () -> verify(mockConnection, times(1)).rollback()
        );
    }

    @DisplayName("오류가 발생하는 runnable의 경우 rollback된다")
    @Test
    void transactionRunnableFail() {
        Runnable exceptionRunnable = () -> {
            throw new RuntimeException("exception");
        };

        assertAll(
                () -> assertThatThrownBy(() -> transactionManager.transaction(exceptionRunnable))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessage("exception"),
                () -> verify(mockConnection, never()).commit(),
                () -> verify(mockConnection, times(1)).rollback()
        );
    }

    @DisplayName("오류가 발생하지 않는 runnable의 경우 commit된다")
    @Test
    void transactionRunnableSuccess() {
        Runnable validConsumer = () -> {
        };

        transactionManager.transaction(validConsumer);

        assertAll(
                () -> verify(mockConnection, times(1)).commit(),
                () -> verify(mockConnection, never()).rollback()
        );
    }
}
