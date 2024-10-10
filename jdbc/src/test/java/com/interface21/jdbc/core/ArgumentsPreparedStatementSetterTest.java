package com.interface21.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArgumentsPreparedStatementSetterTest {

    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);

    @Test
    @DisplayName("파라미터 값을 올바르게 설정한다.")
    void setValues() throws SQLException {
        ArgumentsPreparedStatementSetter preparedStatementSetter = new ArgumentsPreparedStatementSetter("pedro", "aru");
        preparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setObject(1, "pedro");
        verify(preparedStatement).setObject(2, "aru");
    }

    @Test
    @DisplayName("파라미터가 없는 경우, 값을 설정하지 않는다.")
    void setNullValues() throws SQLException {
        ArgumentsPreparedStatementSetter preparedStatementSetter = new ArgumentsPreparedStatementSetter(null);
        preparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, never()).setObject(anyInt(), any());
    }
}
