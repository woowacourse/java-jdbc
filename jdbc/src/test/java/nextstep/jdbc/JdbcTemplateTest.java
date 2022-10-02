package nextstep.jdbc;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

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

    @DisplayName("query 메서드를 실행하였을 때 자원을 모두 반환하였는지 확인한다.")
    @Test
    void closeAllWhenQuery() throws SQLException {
        String sql = "select id, field from testobjects";
        jdbcTemplate.query(sql, rowMapper);

        verify(preparedStatement).executeQuery();
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName("update 메서드를 실행하였을 때 자원을 모두 반환하였는지 확인한다.")
    @Test
    void closeAllWhenUpdate() throws SQLException {
        String sql = "insert into testobjects (field) values (?)";
        jdbcTemplate.update(sql, "filed");

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    private final RowMapper<TestObject> rowMapper = (resultSet, count) -> new TestObject(
            resultSet.getLong("id"),
            resultSet.getString("field2")
    );

    static class TestObject {
        Long id;
        String field;

        public TestObject(Long id, String field) {
            this.id = id;
            this.field = field;
        }
    }
}
