package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class ArgumentPreparedStatementSetterTest {

    @Test
    void setValues() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        ArgumentPreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{1L, "hoho", "password"});
        statementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(3)).setObject(anyInt(), any());
    }

    @Test
    void setNoValues() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        ArgumentPreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{});
        statementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(0)).setObject(anyInt(), any());
    }
}
