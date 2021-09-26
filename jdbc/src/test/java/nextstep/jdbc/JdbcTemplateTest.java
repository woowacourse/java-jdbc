package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private RowMapper rowMapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        rowMapper = mock(RowMapper.class);
        resultSet = mock(ResultSet.class);

        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);
    }

    @Test
    void update() throws SQLException {
        //given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String query = "insert into users (account, password, email) values (?, ?, ?)";

        //when
        jdbcTemplate.update(query, "gugu", "password", "hkkang@woowahan.com");

        //then
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void findAll() throws SQLException {
        //given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select id, account, password, email from users";

        //when
        jdbcTemplate.queryObjects(sql, rowMapper);

        //then
        verify(preparedStatement, times(1)).executeQuery();

    }

    @Test
    void findWithCondition() throws SQLException {
        //given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select id, account, password, email from users where account = ?";

        //when
        jdbcTemplate.queryObjectWithCondition(sql, rowMapper, "gugu");

        //then
        verify(preparedStatement, times(1)).executeQuery();
    }
}