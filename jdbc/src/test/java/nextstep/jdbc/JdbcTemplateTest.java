package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.support.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = (resultSet) -> new TestUser(
            resultSet.getLong("id"),
            resultSet.getString("name")
    );

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement pstmt;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
    }

    @DisplayName("업데이트 쿼리를 실행한다(생성)")
    @Test
    void insert() throws SQLException {
        // given
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "insert into users (name) values (?)";

        // when
        jdbcTemplate.update(sql, "azpi");

        // then
        verify(pstmt, times(1)).setObject(anyInt(), anyString());
        verify(pstmt, times(1)).executeUpdate();
    }

    @DisplayName("업데이트 쿼리를 실행한다(수정)")
    @Test
    void update() throws SQLException {
        // given
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "update users set name = ? where id = ?";

        // when
        jdbcTemplate.update(sql, "azpi", 1L);

        // then
        verify(pstmt, times(1)).setObject(anyInt(), anyString());
        verify(pstmt, times(1)).setObject(anyInt(), anyLong());
        verify(pstmt, times(1)).executeUpdate();
    }

    @DisplayName("단건 조회 쿼리를 실행한다")
    @Test
    void queryForObject() throws SQLException {
        // given
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users where id = ?";
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        // when
        jdbcTemplate.queryForObject(sql, TEST_USER_ROW_MAPPER, 1L);

        // then
        verify(pstmt, times(1)).setObject(anyInt(), anyLong());
        verify(pstmt, times(1)).executeQuery();
    }

    @DisplayName("단건 조회 쿼리를 실행시 값이 없으면 예외를 반환한다.")
    @Test
    void queryForObject_EmptyResult() throws SQLException {
        // given
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users where id = ?";
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        // when && then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_USER_ROW_MAPPER, 1L))
                .isInstanceOf(EmptyResultDataAccessException.class);

        verify(pstmt, times(1)).setObject(anyInt(), anyLong());
        verify(pstmt, times(1)).executeQuery();
    }

    @DisplayName("단건 조회 쿼리를 실행시 값이 여러개면 예외를 반환한다.")
    @Test
    void queryForObject_ManyResult() throws SQLException {
        // given
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users where id = ?";
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        // when && then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_USER_ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);

        verify(pstmt, times(1)).setObject(anyInt(), anyLong());
        verify(pstmt, times(1)).executeQuery();
    }

    @DisplayName("조회 쿼리를 실행한다")
    @Test
    void query() throws SQLException {
        // given
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users";
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        // when
        jdbcTemplate.queryForObject(sql, TEST_USER_ROW_MAPPER, 1L);

        // then
        verify(pstmt, times(1)).executeQuery();
    }
}
