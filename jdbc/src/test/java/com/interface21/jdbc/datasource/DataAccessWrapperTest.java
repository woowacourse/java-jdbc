package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessWrapperTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @DisplayName("에러가 발생할 경우 DataAccessException으로 전환된다.")
    @Test
    void throwDataAccessException_When_ExceptionIsOccurred() {
        DataAccessWrapper accessWrapper = new DataAccessWrapper(dataSource);

        ThrowingBiFunction<Connection, PreparedStatement, Void, Exception> biFunction = (connection, pstmt) -> {
            throw new Exception("throw exception");
        };

        assertThatThrownBy(() -> accessWrapper.apply(biFunction, "dummySql"))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("bifunction의 결과값을 전달한다")
    @Test
    void returnValueOfFunctionResult() {
        DataAccessWrapper accessWrapper = new DataAccessWrapper(dataSource);
        Object dummy = new Object();

        ThrowingBiFunction<Connection, PreparedStatement, Object, Exception> biFunction = (connection, pstmt) -> dummy;

        assertThat(accessWrapper.apply(biFunction, "dummySql")).isEqualTo(dummy);
    }
}
