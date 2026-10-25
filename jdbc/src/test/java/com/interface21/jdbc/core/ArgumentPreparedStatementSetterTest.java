package com.interface21.jdbc.core;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ArgumentPreparedStatementSetterTest {

    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);

    @Test
    void 인자를_1번_파라미터부터_순서대로_바인딩한다() throws SQLException {
        new ArgumentPreparedStatementSetter("gugu", 1L).setValues(preparedStatement);

        final var inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, "gugu");
        inOrder.verify(preparedStatement).setObject(2, 1L);
    }

    @Test
    void null_인자는_setNull로_바인딩한다() throws SQLException {
        new ArgumentPreparedStatementSetter("gugu", null).setValues(preparedStatement);

        verify(preparedStatement).setObject(1, "gugu");
        verify(preparedStatement).setNull(2, Types.NULL);
    }

    @Test
    void 인자가_없으면_아무것도_바인딩하지_않는다() throws SQLException {
        new ArgumentPreparedStatementSetter().setValues(preparedStatement);

        verifyNoInteractions(preparedStatement);
    }
}
