package nextstep.jdbc.core;

import nextstep.jdbc.core.TypeExecutor;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TypeExecutorTest {

    @Test
    void Integer형_파라미터를_지정한다() throws SQLException {
        // given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        TypeExecutor.execute(preparedStatement, 1, 1);

        // then
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void Long형_파라미터를_지정한다() throws SQLException {
        // given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        TypeExecutor.execute(preparedStatement, 1, 1L);

        // then
        verify(preparedStatement).setLong(1, 1L);
    }

    @Test
    void String형_파라미터를_지정한다() throws SQLException {
        // given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        TypeExecutor.execute(preparedStatement, 1, "corinne");

        // then
        verify(preparedStatement).setString(1, "corinne");
    }

    @Test
    void 이외의_파라미터가_전달되면_Object로_지정한다() throws SQLException {
        // given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        TypeExecutor.execute(preparedStatement, 1, 0.12);

        // then
        verify(preparedStatement).setObject(1, 0.12);
    }
}
