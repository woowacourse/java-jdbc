package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource;
    private final Connection connection;
    private final PreparedStatement statement;
    private final ResultSet resultSet;
    private final JdbcTemplate jdbcTemplate;

    JdbcTemplateTest() {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.statement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("getConnection()에서 SQLExceptio 발생 시, DataAccessException으로 변환된다")
    @Test
    void getConnection_throwsSQLException_translateToDataAccessException() throws SQLException {
        final var sql = "select * from users";
        final ObjectMapper<String> objectMapper = (final ResultSet resultSet) -> resultSet.getString(1);

        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.query(sql, objectMapper))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("prepareStatement()에서 SQLException 발생 시, DataAccessException으로 변환된다")
    @Test
    void prepareStatement_throwsSQLException_translateToDataAccessException() throws SQLException {
        final var sql = "select * from users";
        final ObjectMapper<String> objectMapper = (final ResultSet resultSet) -> resultSet.getString(1);

        given(dataSource.getConnection()).willReturn(connection);

        when(connection.prepareStatement(sql)).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.query(sql, objectMapper))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("executeQuery()로 ResultSet 생성하다 SQLException 발생 시, DataAccessException으로 변환된다")
    @Test
    void executeQuery_throwsSQLException_translateToDataAccessException() throws SQLException {
        final var sql = "select * from users where id = ?";
        final var parameter = 1;
        final ObjectMapper<String> objectMapper = (final ResultSet resultSet) -> resultSet.getString(1);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(sql)).willReturn(statement);

        when(statement.executeQuery()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.query(sql, parameter, objectMapper))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("단일 조회 시 resultSet이 비었다면 예외 발생 ")
    @Test
    void query_emptyResultSet_throwsDataAccessException() throws SQLException {
        final var sql = "select * from users where id = ?";
        final var parameter = 1;
        final ObjectMapper<String> objectMapper = (final ResultSet resultSet) -> resultSet.getString(1);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(sql)).willReturn(statement);
        given(statement.executeQuery()).willReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, parameter, objectMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("조회 결과가 존재하지 않습니다.");
    }
    
    @DisplayName("sql에 필요한 파라미터보다 주어진 파라미터가 많다면 예외 발생")
    @Test
    void moreParametersGiven_thenNeededInSql_throwsDataAccessException() throws SQLException {
        final var sql = "insert into users (account, email) values (?, ?)";

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(sql)).willReturn(statement);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "gugu", "gugu@gmail.com", "techcourse"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("sql문 내의 매개변수와 주어진 매개변수의 수가 다릅니다.");
    }

    @DisplayName("sql에 필요한 파라미터보다 주어진 파라미터가 적다면 예외 발생")
    @Test
    void lessParametersGiven_thenNeededInSql_throwsDataAccessException() throws SQLException {
        final var sql = "insert into users (account, email) values (?, ?)";

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(sql)).willReturn(statement);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("sql문 내의 매개변수와 주어진 매개변수의 수가 다릅니다.");
    }
}

