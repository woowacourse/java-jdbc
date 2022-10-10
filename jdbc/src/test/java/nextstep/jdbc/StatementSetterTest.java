package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatementSetterTest {

    @Test
    void Statement_에_속성을_추가한다() throws SQLException {
        // given
        final var statement = mock(PreparedStatement.class);
        final var objects = List.of(1L, "칙촉").toArray();

        // when
        StatementSetter.setValues(statement, objects);

        // then
        verify(statement, times(2)).setObject(anyInt(), any());
    }
}
