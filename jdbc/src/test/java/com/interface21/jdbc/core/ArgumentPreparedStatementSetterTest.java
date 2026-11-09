package com.interface21.jdbc.core;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ArgumentPreparedStatementSetterTest {

    @Test
    void 인자를_순서대로_바인딩한다() throws SQLException {
        final var pstmt = mock(PreparedStatement.class);
        final var pss = new ArgumentPreparedStatementSetter(new Object[]{"gugu", 1L, null});

        pss.setValues(pstmt);

        final InOrder inOrder = inOrder(pstmt);
        inOrder.verify(pstmt).setObject(1, "gugu");
        inOrder.verify(pstmt).setObject(2, 1L);
        inOrder.verify(pstmt).setObject(3, null);
    }

    @Test
    void 인자_배열이_null이면_아무것도_바인딩하지_않는다() throws SQLException {
        final var pstmt = mock(PreparedStatement.class);
        final var pss = new ArgumentPreparedStatementSetter(null);

        pss.setValues(pstmt);

        verifyNoInteractions(pstmt);
    }
}
