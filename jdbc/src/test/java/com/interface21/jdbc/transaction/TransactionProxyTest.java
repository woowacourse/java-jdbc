package com.interface21.jdbc.transaction;

import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TransactionProxyTest {

    private Connection connection;
    private TransactionProxy transactionProxy;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        TestService testService = new TestService();
        transactionProxy = new TransactionProxy(testService, dataSource);
    }

    @DisplayName("메서드 실행 성공 시 트랜잭션을 커밋한다.")
    @Test
    void invokeCommit() throws Throwable {
        Method method = TestService.class.getMethod("testMethod");

        transactionProxy.invoke(null, method, null);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
    }

    @DisplayName("메서드에서 예외 발생 시 트랜잭션을 롤백한다.")
    @Test
    void invokeRollback() throws Throwable {
        Method method = TestService.class.getMethod("testException");

        assertThatThrownBy(() -> transactionProxy.invoke(null, method, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test Exception");

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
    }

    public static class TestService {

        public void testMethod() {
        }

        public void testException() {
            throw new RuntimeException("Test Exception");
        }
    }
}
