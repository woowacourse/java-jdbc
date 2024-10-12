package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class DefaultParameterSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @BeforeEach
    void init() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @Test
    void setParameters() throws SQLException {
        Object[] args = {1, "kaki", "1234", "kaki@email.com"};
        DefaultParameterSetter setter = new DefaultParameterSetter(args);

        setter.setParameters(preparedStatement);

        assertAll(
                () -> verify(preparedStatement).setObject(1, 1),
                () -> verify(preparedStatement).setObject(2, "kaki"),
                () -> verify(preparedStatement).setObject(3, "1234"),
                () -> verify(preparedStatement).setObject(4, "kaki@email.com")
        );
    }
}
