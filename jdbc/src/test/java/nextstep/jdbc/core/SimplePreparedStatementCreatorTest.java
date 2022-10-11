package nextstep.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import org.junit.jupiter.api.Test;

class SimplePreparedStatementCreatorTest {

    @Test
    void createPreparedStatement() {
        Connection connection = mock(Connection.class);
        SimplePreparedStatementCreator psc = new SimplePreparedStatementCreator("SELECT * FROM USER");

        assertDoesNotThrow(() -> psc.createPreparedStatement(connection));
    }
}
