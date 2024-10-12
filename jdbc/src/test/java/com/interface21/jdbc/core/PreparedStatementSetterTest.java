package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PreparedStatementSetterTest {

    @Test
    void PreparedStatement에_파라미터_설정() throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);
        String account = "ted";
        String password = "password";
        String email = "email";

        // when
        PreparedStatementSetter.setParameters(pstmt, account, password, email);

        // then
        assertAll(
                () -> verify(pstmt).setObject(1, "ted"),
                () -> verify(pstmt).setObject(2, "password"),
                () -> verify(pstmt).setObject(3, "email")
        );
    }

    @Test
    void 파라미터가_없을_경우_아무_값도_설정하지_않음() throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);

        // when
        PreparedStatementSetter.setParameters(pstmt);

        // then
        Mockito.verifyNoMoreInteractions(pstmt);
    }

    @Test
    void 파라미터가_null일_걍우_아무_값도_설정하지_않음() throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);

        // when
        PreparedStatementSetter.setParameters(pstmt, null);

        // then
        Mockito.verifyNoMoreInteractions(pstmt);
    }
}
