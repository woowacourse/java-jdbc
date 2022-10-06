package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("connection, preparedStatement를 사용하고 close한다.")
    @Test
    void checkResourceClosed() throws SQLException {
        jdbcTemplate.update("UPDATE users SET password = ? WHERE account = ?",
                ps -> {
                    ps.setObject(1, "1234");
                    ps.setObject(2, "gugu");
                });

        verify(connection).close();
        verify(preparedStatement).close();
    }

    @DisplayName("PreparedStatement을 사용하여 데이터를 insert한다.")
    @Test
    void insertDataUsingPreparedStatement() throws SQLException {
        given(preparedStatement.executeUpdate()).willReturn(1);

        int result = jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                ps -> {
                    ps.setObject(1, "gugu");
                    ps.setObject(2, "1234");
                    ps.setObject(3, "gugu@email.com");
                });

        verify(preparedStatement).executeUpdate();
        assertThat(result).isOne();
    }

    @DisplayName("PreparedStatement을 사용하여 데이터를 find한다.")
    @Test
    void findDataUsingPreparedStatement() throws SQLException {
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        jdbcTemplate.query("SELECT id, account, password, email FROM users WHERE id = ?",
                (rs, rowNum) -> any(), ps -> ps.setObject(1, 1L));

        verify(preparedStatement).executeQuery();
    }

    @DisplayName("queryForObject로 검색했을 때, 검색 결과가 1이 아니면 예외가 발생한다.")
    @Test
    void exceptionQueryForObjectIfResultSizeIsNotOne() throws SQLException {
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        assertThatThrownBy(() ->
                jdbcTemplate.queryForObject("SELECT id, account, password, email FROM users WHERE id = ?",
                        (rs, rowNum) -> any(), ps -> ps.setObject(1, 1L)))
                .isInstanceOf(DataAccessException.class);
    }
}
