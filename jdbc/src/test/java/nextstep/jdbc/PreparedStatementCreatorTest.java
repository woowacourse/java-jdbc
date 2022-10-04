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

class PreparedStatementCreatorTest {

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
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator("SELECT * FROM USER", null);
        assertDoesNotThrow(() -> preparedStatementCreator.createPreparedStatement(connection));
    }

    @Test
    void createPreparedStatement() throws SQLException {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator(
                "SELECT * FROM USER WHERE id = ?", new Object[]{1});
        preparedStatementCreator.createPreparedStatement(connection);
        verify(preparedStatement).setObject(1, 1);
    }
}
