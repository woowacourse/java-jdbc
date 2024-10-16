package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.TransactionRollbackException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionManagerTest {

    private TransactionManager transactionManager;
    private Connection connection;

    @BeforeEach
    void setup() {
        DataSource dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        given(DataSourceUtils.getConnection(dataSource)).willReturn(connection);
        transactionManager = new TransactionManager(dataSource);
    }

    @AfterEach
    void teardown() throws SQLException {
        verify(connection).close();
    }

    @DisplayName("트랜잭션 안에서 동작이 정상적으로 완료되면 커밋 된다.")
    @Test
    void injectTransaction() {
        transactionManager.injectTransaction(conn -> {
        });

        assertAll(
                () -> verify(connection).setAutoCommit(false),
                () -> verify(connection).commit()
        );
    }

    @DisplayName("트랜잭션 내 로직에서 예외가 발생하면 롤백이 동작한다.")
    @Test
    void injectTransactionWhenThrowException() {
        Consumer<Connection> consumer = conn -> {
            throw new DataAccessException();
        };

        assertAll(
                () -> assertThatThrownBy(() -> transactionManager.injectTransaction(consumer))
                        .isInstanceOf(TransactionRollbackException.class)
                        .hasCauseInstanceOf(DataAccessException.class),
                () -> verify(connection).setAutoCommit(false),
                () -> verify(connection).rollback()
        );
    }
}
