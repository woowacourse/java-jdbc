package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PreparedStatement Setter")
class PreparedStatementSetterTest {

    @DisplayName("주어진 인자를 PreparedStatement에 세팅한다.")
    @Test
    void setValue() {
        // given
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Object[] args = new Object[]{"gugu", "gugu@gmail.com", "password"};

        // when
        PreparedStatementSetter.setValue(preparedStatement, args);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, args[0]),
                () -> verify(preparedStatement).setObject(2, args[1]),
                () -> verify(preparedStatement).setObject(3, args[2])
        );
    }
}
