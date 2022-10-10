package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    void queryForObject_결과가_비었다면_에러반환_EmptyResultDataAccessException() throws SQLException {
        //given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        final RowMapper<TestUser> testUserRowMapper = rs ->
                new TestUser(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
        String sql = "select * from users where id = ?";
        given(resultSet.next()).willReturn(false);

        //when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, testUserRowMapper, 1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void queryForObject_결과가_하나_이상이라면_에러반환_EmptyResultDataAccessException() throws SQLException {
        //given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        final RowMapper<TestUser> testUserRowMapper = rs ->
                new TestUser(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
        String sql = "select * from users where id = ?";
        given(resultSet.next()).willReturn(true, true, true, false);

        //when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, testUserRowMapper, 1L))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
