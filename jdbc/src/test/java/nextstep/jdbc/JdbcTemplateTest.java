package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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

    private Connection connection;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate template;

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.template = new JdbcTemplate(this.dataSource);
        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }

    @Test
    void update() throws SQLException {
        final String sql = "query";
        int expectedRowsAffected = 1;

        given(this.preparedStatement.executeUpdate()).willReturn(expectedRowsAffected);

        int actualRowsAffected = template.update(sql);
        assertThat(actualRowsAffected).isEqualTo(expectedRowsAffected);
        verify(this.connection).close();
    }

    @Test
    void query() throws SQLException {
        final var sql = "query";

        given(resultSet.next()).willReturn(false);

        List<Object> result = template.query(sql, (resultSet, rowNum) -> "test");

        assertThat(result).isEmpty();
    }

    @Test
    void queryForObject() throws SQLException {
        final var sql = "query";

        given(resultSet.next()).willReturn(true);
        Object actual = template.queryForObject(sql, (resultSet, rowNum) -> new Object());

        assertThat(actual).isNotNull();
    }
}
