package nextstep.jdbc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PreparedStatementSetterTest {

    private List<PreparedStatementSetter> pstmtSetters;
    private PreparedStatement pstmt;

    @BeforeEach
    void setUp() {
        this.pstmt = mock(PreparedStatement.class);
        this.pstmtSetters = new ArrayList<>();
        pstmtSetters.add(new StringSetter());
        pstmtSetters.add(new LongSetter());
    }

    @Test
    void stringInstance() throws SQLException {
        for (PreparedStatementSetter setter : pstmtSetters) {
            setter.set(pstmt, 1, "string");
        }

        verify(pstmt, times(1)).setString(1, "string");
    }

    @Test
    void longInstance() throws SQLException {
        for (PreparedStatementSetter setter : pstmtSetters) {
            setter.set(pstmt, 1, 1L);
        }

        verify(pstmt, times(1)).setLong(1, 1L);
    }
}
