package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PreparedStatementResolverTest {

    @DisplayName("PreparedStatement의 인자를 설정한다.")
    @Test
    void setParameters() throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);
        String arg1 = "value1";
        String arg2 = "value2";

        // when
        PreparedStatementResolver.setParameters(pstmt, arg1, arg2);

        // then
        assertAll(
                () -> verify(pstmt).setObject(1, arg1),
                () -> verify(pstmt).setObject(2, arg2)
        );
    }

    @DisplayName("PreparedStatement의 인자로 전달받은 값이 없으면 아무 예외도 발생하지 않는다.")
    @Test
    void doesNotSetParameters() {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);

        // when & then
        assertDoesNotThrow(() -> PreparedStatementResolver.setParameters(pstmt, null));
    }
}
