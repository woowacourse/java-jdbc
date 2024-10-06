package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class PreparedStatementResolverTest {

    @DisplayName("PreparedStatement에 파라미터 값을 세팅해준다")
    @MethodSource("provideParameters")
    @ParameterizedTest
    void resolve(Object parameter1, Object parameter2) throws SQLException {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        PreparedStatementResolver resolver = new PreparedStatementResolver();
        PreparedStatement statement = mock(PreparedStatement.class);

        doNothing()
                .when(statement)
                .setObject(anyInt(), captor.capture());

        resolver.resolve(statement, parameter1, parameter2);

        assertThat(captor.getAllValues())
                .containsExactly(parameter1, parameter2);
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("test1", "test2"),
                Arguments.of(1L, 2L),
                Arguments.of(new Object(), new Object())
        );
    }
}
