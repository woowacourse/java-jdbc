package nextstep.jdbc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArgumentPreparedStatementSetterTest {

    @Test
    @DisplayName("setValues 메서드는 PreparedStatement에 arguement를 삽입한다.")
    void setValues() throws SQLException {
        // given
        final Object[] args = {"pepper"};
        final PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(args);
        final PreparedStatement statement = mock(PreparedStatement.class);

        // when
        preparedStatementSetter.setValues(statement);

        // then
        verify(statement).setObject(1, "pepper");
    }
}
