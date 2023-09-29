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
        Mockito.when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        final var jdbcTemplate = new JdbcTemplate(mockDataSource);

        //when
        jdbcTemplate.execute("insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)", 1L, "account", "password", "email",
                "2021-08-10 00:00:00", "admin");

        //then
        Mockito.verify(mockDataSource, Mockito.only()).getConnection();
        Mockito.verify(mockConnection, Mockito.times(1)).prepareStatement(Mockito.anyString());
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(1, 1L);
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(2, "account");
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(3, "password");
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(4, "email");
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(5, "2021-08-10 00:00:00");
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(6, "admin");
        Mockito.verify(mockPreparedStatement, Mockito.times(1)).executeUpdate();
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
                () -> Mockito.verify(mockDataSource, Mockito.only()).getConnection(),
                () -> Mockito.verify(mockConnection, Mockito.times(1)).prepareStatement(Mockito.anyString()),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(1, 1L),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).executeQuery(),
                () -> Mockito.verify(mockResultSet, Mockito.times(2)).next(),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getObject("user_id"),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getObject("account"),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getObject("password"),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getObject("email"),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getObject("created_at"),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getObject("created_by"),
                () -> assertThat(params).containsExactly(1L, "account", "password", "email", "2021-08-10 00:00:00", "admin")
        );
    }
}
