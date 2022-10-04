package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement pstmt;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(pstmt);
    }

    @Test
    void update() throws SQLException {
        // given
        final String sql = "insert into user values (?, ?, ?)";
        when(pstmt.executeUpdate()).thenReturn(1);

        // when
        final int insertUserId = jdbcTemplate.update(sql, "bunny", "1234", "bunny@test.com");

        assertThat(insertUserId).isOne();
        verify(pstmt).setObject(1, "bunny");
        verify(pstmt).setObject(2, "1234");
        verify(pstmt).setObject(3, "bunny@test.com");
        verify(connection).close();
    }
}
