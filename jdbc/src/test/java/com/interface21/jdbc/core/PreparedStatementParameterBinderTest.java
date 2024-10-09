package com.interface21.jdbc.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementParameterBinderTest {

    @Test
    @DisplayName("PreparedStatement로 SQL에 파라미터를 바인딩한다.")
    void bindStatementParameters() throws Exception {
        // given
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        PreparedStatementParameterBinder.bindStatementParameters(preparedStatement,
                new Object[]{"mia", "password", "mia@gmail.com"});

        // then
        verify(preparedStatement).setObject(1, "mia");
        verify(preparedStatement).setObject(2, "password");
        verify(preparedStatement).setObject(3, "mia@gmail.com");
    }
}
