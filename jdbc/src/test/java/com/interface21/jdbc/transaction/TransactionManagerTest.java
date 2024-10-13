package com.interface21.jdbc.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    @DisplayName("트랜잭션 실행 중 예외가 발생하면 롤백된다.")
    void rollbackOnException() throws SQLException {
        // given
        TransactionManager txManager = new TransactionManager(dataSource);
        TransactionalFunction txFunction = conn -> {
            throw new SQLException();
        };

        // when & then
        assertThatThrownBy(() -> txManager.executeTransactionOf(txFunction))
                .isInstanceOf(DataAccessException.class);
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    @DisplayName("예외 없는 트랜잭션은 정상적으로 커밋된다.")
    void commitOnNoException() throws SQLException {
        TransactionManager txManager = new TransactionManager(dataSource);
        TransactionalFunction txFunction = conn -> {};
        txManager.executeTransactionOf(txFunction);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }
}
