package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ArgumentsPreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @BeforeEach
    void init() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @DisplayName("인자를 받아 preparedStatement의 파라미터를 세팅한다.")
    @Test
    void setParameters() throws SQLException {
        Object[] args = {1, "kaki", "1234", true};
        ArgumentsPreparedStatementSetter setter = new ArgumentsPreparedStatementSetter(args);

        setter.setParameters(preparedStatement);

        assertAll(
                () -> verify(preparedStatement).setObject(1, 1),
                () -> verify(preparedStatement).setObject(2, "kaki"),
                () -> verify(preparedStatement).setObject(3, "1234"),
                () -> verify(preparedStatement).setObject(4, true)
        );
    }
}
