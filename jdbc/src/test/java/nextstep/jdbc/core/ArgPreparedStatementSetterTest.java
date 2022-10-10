package nextstep.jdbc.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class ArgPreparedStatementSetterTest {

    @Test
    void setValues() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        ArgPreparedStatementSetter pss = new ArgPreparedStatementSetter(new Object[]{1, 2, 3});
        pss.setValues(preparedStatement);

        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 2);
        verify(preparedStatement).setObject(3, 3);
    }
}
