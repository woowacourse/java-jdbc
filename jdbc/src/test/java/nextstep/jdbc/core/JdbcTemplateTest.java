package nextstep.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Crew> rowMapper = (rs, rowNum) -> new Crew(
            resultSet.getLong("id"),
            resultSet.getString("name")
    );

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void closeResourcesWhenQuery() throws SQLException {
        jdbcTemplate.query("select * from crew", rowMapper);

        verify(preparedStatement).executeQuery();
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    void closeResourcesWhenUpdate() throws SQLException {
        jdbcTemplate.update("update crew set name = lala where id = 1", rowMapper);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    static class Crew {
        Long id;
        String name;

        public Crew(final Long id, final String name) {
            this.id = id;
            this.name = name;
        }
    }
}
