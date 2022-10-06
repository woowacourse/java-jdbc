package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class SimplePreparedStatementStarterTest {

    @Test
    void execute_update를_실행하면_int를_반환한다() throws SQLException {
        // given
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(3);

        SimplePreparedStatementStarter pss = new SimplePreparedStatementStarter(ps);
        // when
        int updatedQueryCount = pss.executeUpdate();

        // then
        assertThat(updatedQueryCount).isEqualTo(3);
    }

    @Test
    void execute_query를_실행하면_result_set을_반환한다() throws SQLException {
        // given
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.getString("name")).thenReturn("eden");

        SimplePreparedStatementStarter pss = new SimplePreparedStatementStarter(ps);
        // when
        ResultSet resultSet = pss.executeQuery();

        // then
        assertThat(resultSet.getString("name")).isEqualTo("eden");
    }
}
