package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.exception.PreparedStatementSetFailureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementValueSetterTest {

    @DisplayName("PreparedStatement에 값을 하나 넣는다 - 실패")
    @Test
    void whenSetPreparedStatmentValue_thenThrowPreparedStatementSetFailureException()
        throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);
        PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
        Object arg = new Object();
        doThrow(new SQLException()).when(pstmt).setObject(1, arg);

        // when // then
        assertThatThrownBy(() -> valueSetter.setPreparedStatementValue(arg))
            .isExactlyInstanceOf(PreparedStatementSetFailureException.class);
    }

    @DisplayName("PreparedStatement에 값을 여러 개 넣는다 - 실패")
    @Test
    void whenSetPreparedStatementValues_thenThrowPreparedStatementFailureException()
        throws SQLException {
        // given
        PreparedStatement pstmt = mock(PreparedStatement.class);
        PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
        Object arg = new Object();
        Object[] args = new Object[]{arg};
        doThrow(new SQLException()).when(pstmt).setObject(1, arg);

        // when // then
        assertThatThrownBy(() -> valueSetter.setPreparedStatementValues(args))
            .isExactlyInstanceOf(PreparedStatementSetFailureException.class);
    }
}
