package nextstep.jdbc.jdbcparam;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcParamTypeTest {

    @Test
    @DisplayName("Integer 형이 올 경우 PreparedStatement의 setInt를 호출한다.")
    void setParam() throws SQLException {
        // given
        PreparedStatement statement = mock(PreparedStatement.class);
        Object param = 1;

        // when
        JdbcParamType.setParam(statement, 1, param);

        // then
        verify(statement).setInt(1, 1);
    }

    @Test
    @DisplayName("Long 형이 올 경우 PreparedStatement의 setLong을 호출한다.")
    void setParam_long() throws SQLException {
        // given
        PreparedStatement statement = mock(PreparedStatement.class);
        Object param = 1000000000000000000L;

        // when
        JdbcParamType.setParam(statement, 1, param);

        // then
        verify(statement).setLong(1, 1000000000000000000L);
    }

    @Test
    @DisplayName("문자열이 올 경우 PreparedStatement의 setString을 호출한다.")
    void setParam_string() throws SQLException {
        // given
        PreparedStatement statement = mock(PreparedStatement.class);
        Object param = "hello";

        // when
        JdbcParamType.setParam(statement, 1, param);

        // then
        verify(statement).setString(1, "hello");
    }
}
