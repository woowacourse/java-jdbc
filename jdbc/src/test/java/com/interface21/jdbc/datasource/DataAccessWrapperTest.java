package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessWrapperTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private DataSource dataSource;
    private DataAccessWrapper accessWrapper;

    @BeforeEach
    public void setUp() throws SQLException {
        connection = mock(Connection.class);
        dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        accessWrapper = new DataAccessWrapper(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @DisplayName("에러가 발생할 경우 DataAccessException으로 전환된다.")
    @Test
    void throwDataAccessException_When_ExceptionIsOccurred() {
        ThrowingFunction<PreparedStatement, Void, Exception> function = (pstmt) -> {
            throw new Exception("throw exception");
        };

        assertThatThrownBy(() -> accessWrapper.apply("dummySql", function))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("bifunction의 결과값을 전달한다")
    @Test
    void returnValueOfFunctionResult() {
        Object dummy = new Object();

        ThrowingFunction<PreparedStatement, Object, Exception> function = (pstmt) -> dummy;

        assertThat(accessWrapper.apply("dummySql", function)).isEqualTo(dummy);
    }
}