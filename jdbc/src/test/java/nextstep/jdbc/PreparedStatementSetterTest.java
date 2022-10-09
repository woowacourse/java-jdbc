package nextstep.jdbc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementSetterTest {

    @DisplayName("PreparedStatementSetter 콜백 인터페이스를 통해 PreparedStatement의 값을 업데이트한다")
    @Test
    void updatePreparedStatementByPreparedStatementSetter() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementSetter preparedStatementSetter = ps -> ps.setObject(1, 2);
        preparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setObject(1, 2);
    }
}
