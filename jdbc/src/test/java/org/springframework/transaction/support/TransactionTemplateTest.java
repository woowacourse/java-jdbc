package org.springframework.transaction.support;

import nextstep.jdbc.TestDataSourceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionTemplateTest {

    private DataSource dataSource;
    private TransactionTemplate transactionTemplate;

    @DisplayName("TransactionCallback을 실행하고 결과를 반환한다.")
    @Test
    void execute_Success() {
        // given
        dataSource = TestDataSourceConfig.createJdbcDataSource();
        transactionTemplate = new TransactionTemplate(dataSource);

        // when & then
        String result = transactionTemplate.execute(() -> "Transaction Successful");
        assertEquals("Transaction Successful", result);
    }

    @DisplayName("Runnable을 실행하고 결과를 반환하지 않는다.")
    @Test
    void executeWithoutResult_Success() {
        // given
        dataSource = TestDataSourceConfig.createJdbcDataSource();
        transactionTemplate = new TransactionTemplate(dataSource);

        // when & then
        assertDoesNotThrow(() -> transactionTemplate.executeWithoutResult(() -> {}));
    }

    @DisplayName("TransactionCallback 실행 중 SQLException이 발생하면 DataAccessException을 던지고 트랜잭션을 롤백한다.")
    @Test
    void execute_FailTestWithMock() throws SQLException {
        // given
        dataSource = mock(DataSource.class);
        transactionTemplate = new TransactionTemplate(dataSource);

        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);

        TransactionCallback throwExceptionLogic = () -> {
            throw new SQLException();
        };

        // when & then
        assertThatThrownBy(() -> transactionTemplate.execute(throwExceptionLogic))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Failed to change password.");
        verify(mockConnection, times(1)).rollback();
        verify(mockConnection, times(1)).close();
    }
}
