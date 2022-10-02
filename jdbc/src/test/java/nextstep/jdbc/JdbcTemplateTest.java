package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @DisplayName("JdbcTemplate 이 연결된 connection을 반환해줄 수 있다.")
    @Test
    void getConnection() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        final Connection gottenConnection = jdbcTemplate.getConnection();

        // then
        assertThat(gottenConnection).isEqualTo(connection);
        verify(dataSource).getConnection();
    }
}
