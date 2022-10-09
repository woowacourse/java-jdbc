package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PreparedStatementSetterTest {

    private Connection connection;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() throws Exception {
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);

        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
    }

    @Test
    void createPreparedStatementWhenNull() throws SQLException {
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter("SELECT * FROM USER", null);
        assertDoesNotThrow(() -> preparedStatementSetter.createPreparedStatement(connection));
    }

    @Test
    void createPreparedStatement() throws SQLException {
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(
                "SELECT * FROM USER WHERE id = ?", new Object[]{1});
        preparedStatementSetter.createPreparedStatement(connection);
        verify(preparedStatement).setObject(1, 1);
    }
}
