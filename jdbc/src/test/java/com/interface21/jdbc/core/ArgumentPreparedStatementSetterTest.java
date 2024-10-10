package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArgumentPreparedStatementSetterTest {

    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @Test
    void PreparedStatement에_인자를_세팅한다() throws SQLException {
        // given
        Object[] args = new Object[] {1L, "test"};
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);

        // when
        pss.setValues(preparedStatement);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, 1L),
                () -> verify(preparedStatement).setObject(2, "test")
        );
    }

    @Test
    void 인자가_null일_경우_세팅하지_않는다() throws SQLException {
        // given
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(null);

        // when
        pss.setValues(preparedStatement);

        // then
        verifyNoInteractions(preparedStatement);
    }
}
