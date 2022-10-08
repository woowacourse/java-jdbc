package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateObjects() throws Exception {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);

        JdbcUser user = new JdbcUser("hoho", "password", "hoho@email.com");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(sql);
        verify(preparedStatement, times(1)).setObject(1, "hoho");
        verify(preparedStatement, times(1)).setObject(2, "password");
        verify(preparedStatement, times(1)).setObject(3, "hoho@email.com");
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void update() throws Exception {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);

        JdbcUser jdbcUser = new JdbcUser("hoho", "password", "hoho@email.com");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, jdbcUser.getAccount(), jdbcUser.getPassword(), jdbcUser.getEmail());

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(sql);
        verify(preparedStatement, times(1)).setObject(1, "hoho");
        verify(preparedStatement, times(1)).setObject(2, "password");
        verify(preparedStatement, times(1)).setObject(3, "hoho@email.com");
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void queryForList() throws Exception {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, true, false);
        given(resultSet.getLong("id")).willReturn(1L, 2L, 3L);
        given(resultSet.getString("account")).willReturn("hoho", "gugu", "pang");
        given(resultSet.getString("password")).willReturn("password");
        given(resultSet.getString("email")).willReturn("email@email.com");

        String sql = "select * from users";

        List<JdbcUser> users = jdbcTemplate.query(sql, rs -> new JdbcUser(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")));

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(sql);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(4)).next();
        assertThat(users).hasSize(3);
    }

    @Test
    void queryForObject() throws Exception {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getLong("id")).willReturn(1L);
        given(resultSet.getString("account")).willReturn("hoho");
        given(resultSet.getString("password")).willReturn("password");
        given(resultSet.getString("email")).willReturn("email@email.com");

        String sql = "select * from users where id = ?";

        JdbcUser user = jdbcTemplate.query(sql, rs -> new JdbcUser(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")), ps -> {
            ps.setLong(1, 1L);
        });

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(sql);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(2)).next();
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void executeUpdate() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);

        jdbcTemplate.execute("delete from jdbc_user");

        verify(preparedStatement, times(1)).execute();
    }
}
