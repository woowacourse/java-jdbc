package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class JdbcTemplateTest {

    private ResultSet resultSet;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.resultSet = mock(ResultSet.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @DisplayName("JdbcTemplate를 이용해 DB로부터 하나의 객체를 조회한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        final String sql = "select id, account from user where id = ?";
        when(resultSet.next()).thenReturn(true, false);

        // when
        final TestUser actual = jdbcTemplate.queryForObject(sql, rs ->
                new TestUser(1L, "sun"), 1L);

        // then
        assertAll(
                () -> assertThat(actual).usingRecursiveComparison().isEqualTo(new TestUser(1, "sun")),
                () -> verify(preparedStatement).setObject(1, 1L),
                () -> verify(preparedStatement).executeQuery(),
                () -> verify(resultSet).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }

    @DisplayName("JdbcTemplate를 이용해 DB로부터 하나의 객체를 조회할 때, 반환된 객체가 여러 개라면 예외가 발생한다.")
    @Test
    void queryForObject_throwsException_ifMultipleObjects() throws SQLException {
        // given
        final String sql = "select id, account from user where account = ?";
        when(resultSet.next()).thenReturn(true, true, false);

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs ->
                new TestUser(rs.getLong("id"), rs.getString("account")), List.of(1L, 2L)))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("하나 이상의 데이터가 존재합니다.");
    }

    @DisplayName("JdbcTemplate를 이용해 DB로부터 객체를 조회한다.")
    @Test
    void query() throws SQLException {
        // given
        final String sql = "select id, account from user where account = ?";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("account")).thenReturn("sun", "sun");

        // when
        final List<TestUser> actual = jdbcTemplate.query(sql, rs ->
                new TestUser(rs.getLong("id"), rs.getString("account")), List.of(1L, 2L));

        // then
        assertAll(
                () -> assertThat(actual).usingRecursiveComparison()
                        .isEqualTo(List.of(new TestUser(1, "sun"), new TestUser(2, "sun"))),
                () -> verify(preparedStatement).setObject(1, List.of(1L, 2L)),
                () -> verify(preparedStatement).executeQuery(),
                () -> verify(resultSet).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }

    @DisplayName("JdbcTemplate를 이용해 DB에 저장한다.")
    @Test
    void update() throws SQLException {
        // given
        final String sql = "insert into user values (?, ?)";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // when
        final int insertedRows = jdbcTemplate.update(sql, 1L, "sun");

        // then
        assertAll(
                () -> assertThat(insertedRows).isOne(),
                () -> verify(preparedStatement).setObject(1, 1L),
                () -> verify(preparedStatement).setObject(2, "sun"),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }
}
