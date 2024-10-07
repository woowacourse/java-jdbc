package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PreparedStatementResolverTest {

    @DisplayName("PreparedStatement의 인자를 설정한다.")
    @Test
    void setParameters() {
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
}
