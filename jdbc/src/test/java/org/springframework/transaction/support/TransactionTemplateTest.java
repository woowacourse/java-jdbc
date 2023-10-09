package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.test_supporter.DataSourceConfig;

class TransactionTemplateTest {

    private Connection connection;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = Mockito.spy(DataSourceConfig.getInstance());
        connection = Mockito.spy(dataSource.getConnection());
        given(dataSource.getConnection()).willReturn(connection);
        transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Test
    @DisplayName("반환값이 있는 메서드를 트랜잭션 단위로 동작시킨다.")
    void executeWitResult() throws SQLException {
        //when
        assertThatThrownBy(
            () -> transactionTemplate.executeWithResult(this::throwExceptionSupplier));

        //then
        verify(connection, times(1)).rollback();
    }

    @Test
    @DisplayName("반환값이 없는 메서드를 트랜잭션 단위로 동작시킨다.")
    void execute() throws SQLException {
        //when
        assertThatThrownBy(() -> transactionTemplate.execute(this::throwExceptionSupplier));

        //then
        verify(connection, times(1)).rollback();
    }

    private Object throwExceptionSupplier() {
        throw new RuntimeException();
    }

    private void throwExceptionRunnable() {
        throw new RuntimeException();
    }
}
