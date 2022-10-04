package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class JdbcTemplateTest {

    private static final RowMapper<String> TEST_ROW_MAPPER = (rs, rowNum) -> "test";
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void set() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("SQL에 args를 매핑하여 쿼리문을 만들고 실행시키는지 확인한다.")
    void query() throws SQLException {
        final String testSql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(testSql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        jdbcTemplate.query(testSql, TEST_ROW_MAPPER, "test arg");

        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(connection).prepareStatement(testSql),
                () -> verify(preparedStatement).setObject(anyInt(), any()),
                () -> verify(resultSet).next()
        );

    }

    @Test
    @DisplayName("단일 결과 쿼리문 수행여부를 확인한다.")
    void queryForObject() throws SQLException {
        final String testSql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(testSql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getRow()).thenReturn(1);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        final Object actual = jdbcTemplate.queryForObject(testSql, TEST_ROW_MAPPER, "test args");

        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> verify(preparedStatement).setObject(anyInt(), any())
        );
    }

    @Test
    @DisplayName("단일 결과 쿼리문 수행 중 조회 결과가 없으면 Exception을 발생시킨다.")
    void queryForObject_noResult() throws SQLException {
        final String testSql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(testSql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(testSql, TEST_ROW_MAPPER, "test args"))
                        .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("단일 결과 쿼리문 수행 중 조획 결과가 1개 이상일 경우 Exception을 발생시킨다.")
    void queryForObject_moreThanOne() throws SQLException {
        final String testSql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(testSql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(testSql, TEST_ROW_MAPPER, "test args"))
                        .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("업데이트 쿼리를 수행여부를 확인한다.")
    void executeUpdate() throws SQLException {
        final String testSql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);

        jdbcTemplate.executeUpdate(testSql, "update arg");
        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(connection).prepareStatement(testSql),
                () -> verify(preparedStatement).setObject(anyInt(), any(String.class))
        );
    }
}
