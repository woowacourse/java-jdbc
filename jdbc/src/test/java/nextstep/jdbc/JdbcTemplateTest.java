package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

class JdbcTemplateTest {

    @Test
    void execute() throws SQLException {

        //given
        final var mockDataSource = Mockito.mock(DataSource.class);
        final var mockConnection = Mockito.mock(Connection.class);
        final var mockPreparedStatement = Mockito.mock(PreparedStatement.class);

        Mockito.when(DataSourceUtils.getConnection(mockDataSource)).thenReturn(mockConnection);
        Mockito.when(mockDataSource.getConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.execute()).thenReturn(true);

        final var jdbcTemplate = new JdbcTemplate(mockDataSource);

        //when
        jdbcTemplate.execute("insert into user_history (user_id, account) values (?, ?)", 1L, "account");

        //then
        assertAll(
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(1, 1L),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(2, "account"),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).execute()
        );
    }

    @Test
    void query() throws SQLException {
        //given
        final var mockDataSource = Mockito.mock(DataSource.class);
        final var mockConnection = Mockito.mock(Connection.class);
        final var mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final var mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(mockDataSource.getConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(mockResultSet.getObject(Mockito.anyString())).thenReturn(1L).thenReturn("account").thenReturn("password").thenReturn("email").thenReturn("2021-08-10 00:00:00")
               .thenReturn("admin");

        final var jdbcTemplate = new JdbcTemplate(mockDataSource);
        final var params = new ArrayList<>();

        //when
        jdbcTemplate.query("select * from user_history where user_id = ?", rs -> {
            params.add(rs.getObject("user_id"));
            params.add(rs.getObject("account"));
            params.add(rs.getObject("password"));
            params.add(rs.getObject("email"));
            params.add(rs.getObject("created_at"));
            params.add(rs.getObject("created_by"));
            return null;
        }, 1L);

        //then
        assertAll(
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(1, 1L),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).executeQuery(),
                () -> Mockito.verify(mockResultSet, Mockito.times(2)).next(),
                () -> Mockito.verify(mockResultSet, Mockito.times(6)).getObject(Mockito.any()),
                () -> assertThat(params).containsExactly(1L, "account", "password", "email", "2021-08-10 00:00:00", "admin")
        );
    }
}
