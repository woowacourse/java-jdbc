package com.interface21.jdbc.core.utils;

import com.interface21.jdbc.core.PreparedStatementSetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.interface21.jdbc.core.utils.DefaultPreparedStatementSetterFactory.createDefaultPreparedStatementSetter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultPreparedStatementSetterFactoryTest {

    private final Object[] parameters = {"ash", "backend"};
    private PreparedStatement preparedStatement;
    private ParameterMetaData parameterMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        preparedStatement = mock(PreparedStatement.class);
        parameterMetaData = mock(ParameterMetaData.class);

        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
    }

    @Test
    void testCreateDefaultPreparedStatementSetter_withValidParameters() throws Exception {
        // given
        when(parameterMetaData.getParameterCount()).thenReturn(2);

        // when, then
        assertAll(
                () -> assertDoesNotThrow(() -> {
                    PreparedStatementSetter setter = createDefaultPreparedStatementSetter(parameters);
                    setter.setParameters(preparedStatement);
                }),
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any()),
                () -> verify(preparedStatement).setObject(1, "ash"),
                () -> verify(preparedStatement).setObject(2, "backend")
        );
    }

    @Test
    void testCreateDefaultPreparedStatementSetter_withInvalidParameterCount() throws Exception {
        // given
        when(parameterMetaData.getParameterCount()).thenReturn(1);

        // when, then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PreparedStatementSetter setter = createDefaultPreparedStatementSetter(parameters);
            setter.setParameters(preparedStatement);
        });

        assertAll(
                () -> assertThat(exception).isInstanceOf(IllegalArgumentException.class),
                () -> assertThat(exception.getMessage()).contains("파라미터 개수")
        );
    }
}
