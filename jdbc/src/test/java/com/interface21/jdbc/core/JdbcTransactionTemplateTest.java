package com.interface21.jdbc.core;

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

class JdbcTransactionTemplateTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    @DisplayName("트랜잭션 내부에서 예외가 발생하면 롤백한다.")
    void rollbackOnException() throws SQLException {
        JdbcTransactionTemplate template = new JdbcTransactionTemplate(dataSource);
        TransactionalCallback callback = conn -> {
            throw new RuntimeException();
        };
        assertThatThrownBy(() -> template.executeTransactional(callback))
                .isInstanceOf(DataAccessException.class);

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    @DisplayName("트랜잭션이 성공하면 커밋된다.")
    void commitOnNoException() throws SQLException {
        JdbcTransactionTemplate template = new JdbcTransactionTemplate(dataSource);
        TransactionalCallback callback = conn -> {};
        template.executeTransactional(callback);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }
}
