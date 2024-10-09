package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArgumentPreparedStatementSetterTest {

    @DisplayName("PreparedStatement가 파라미터의 순서대로 값을 잘 저장한다.")
    @Test
    void testSetValues() throws SQLException {
        // given
        final Object[] params = { 1L, "a", "b", "c" };
        final PreparedStatementSetter pstmtSetter = new ArgumentPreparedStatementSetter(params);
        final PreparedStatement pstmt = mock(PreparedStatement.class);

        // when
        pstmtSetter.setValues(pstmt);

        // then
        assertAll(
                () -> verify(pstmt).setObject(1, params[0]),
                () -> verify(pstmt).setObject(2, params[1]),
                () -> verify(pstmt).setObject(3, params[2]),
                () -> verify(pstmt).setObject(4, params[3])
        );
    }
}
