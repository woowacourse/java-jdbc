package com.interface21.jdbc.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TypedPreparedStatementSetterTest {

    private PreparedStatement preparedStatement;
    private TypedPreparedStatementSetter typedPreparedStatementSetter;

    @BeforeEach
    public void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @Test
    @DisplayName("여러 값을 올바르게 설정한다.")
    public void setMultipleValues() throws SQLException {
        typedPreparedStatementSetter = new TypedPreparedStatementSetter(1, "test", true, 3.14, 100L);
        typedPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "test");
        verify(preparedStatement).setBoolean(3, true);
        verify(preparedStatement).setDouble(4, 3.14);
        verify(preparedStatement).setLong(5, 100L);
    }

    @Test
    @DisplayName("설정되지 않은 타입에 대해서는 setObject를 호출한다.")
    public void setNotSupportedValue() throws SQLException {
        LocalDate localDate = LocalDate.now();
        typedPreparedStatementSetter = new TypedPreparedStatementSetter(localDate);
        typedPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setObject(1, localDate);
    }
}
