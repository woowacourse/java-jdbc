package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementSetterTest {

    @DisplayName("PreparedStatement 매개변수 값이 널이면 예외가 발생한다.")
    @Test
    void setValuesToNull() {
        assertThatThrownBy(() -> new PreparedStatementSetter(null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("PreparedStatement 매개변수 값을 지정할 수 있다.")
    @Test
    void setValues() throws SQLException {
        final var pstmt = mock(PreparedStatement.class);

        final Object[] args = {1L, "jerry", null};
        final var pss = new PreparedStatementSetter(args);

        pss.setValues(pstmt);

        verify(pstmt).setObject(1, 1L);
        verify(pstmt).setObject(2, "jerry");
        verify(pstmt).setObject(3, null);
        verifyNoMoreInteractions(pstmt);
    }
}
