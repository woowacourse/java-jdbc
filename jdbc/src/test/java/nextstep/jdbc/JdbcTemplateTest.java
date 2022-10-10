package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.calls;
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
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.executeQuery(anyString())).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }

    @Test
    void update() throws SQLException {
        //given
        final String sql = "query";
        int expectedRowsAffected = 1;

        given(this.preparedStatement.executeUpdate())
                .willReturn(expectedRowsAffected);

        //when
        int actualRowsAffected = jdbcTemplate.update(sql);

        //then
        assertAll(
                () -> assertThat(actualRowsAffected).isEqualTo(expectedRowsAffected),
                () -> verify(this.connection).close()
        );
    }

    @Test
    void queryForList() throws SQLException {
        //given
        final var sql = "query";
        given(resultSet.next()).willReturn(false);

        //when
        List<Object> result = jdbcTemplate.queryForList(sql, (resultSet, rowNum) -> "test");

        //then
        assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(preparedStatement.executeQuery(), calls(1))
        );
    }

    @Test
    void queryForObject() throws SQLException {
        //given
        final var sql = "query";

        given(resultSet.next()).willReturn(true);

        //when
        Object actual = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> new Object());

        //then
        assertThat(actual).isNotNull();
        verify(preparedStatement.executeQuery(), calls(1));
    }
}
