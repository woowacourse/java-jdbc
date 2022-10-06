package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(pstmt);
    }

    @Test
    void update() throws SQLException {
        // given
        final String sql = "insert into user values (?, ?, ?)";
        when(pstmt.executeUpdate()).thenReturn(1);

        // when
        final int insertUserId = jdbcTemplate.update(sql, "bunny", "1234", "bunny@test.com");

        assertThat(insertUserId).isOne();
        verify(pstmt).setObject(1, "bunny");
        verify(pstmt).setObject(2, "1234");
        verify(pstmt).setObject(3, "bunny@test.com");
        verify(connection).close();
    }

    @DisplayName("한건의 데이터를 조회하여 결과를 받을 수 있다.")
    @Test
    void queryForObject() throws SQLException {
        //  given
        resultSet = mock(ResultSet.class);
        String sql = "select * from users where id = ?";
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("bunny");
        when(resultSet.getString("password")).thenReturn("1234");
        when(resultSet.getString("email")).thenReturn("bunny@test.com");

        // when
        FakeUser fakeUser = jdbcTemplate.queryForObject(sql, testRowMapper(), 1L);

        assertThat(fakeUser.getId()).isEqualTo(1L);
        assertThat(fakeUser.getAccount()).isEqualTo("bunny");
        assertThat(fakeUser.getPassword()).isEqualTo("1234");
        assertThat(fakeUser.getEmail()).isEqualTo("bunny@test.com");
    }

    @DisplayName("queryForObject로 조회 시 여러 건이 조회된다면 예외가 발생한다.")
    @Test
    void queryForObject_WhenFindMoreThanTwo() throws SQLException {
        //  given
        resultSet = mock(ResultSet.class);
        String sql = "select * from users where id = ?";
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 1L);
        when(resultSet.getString("account")).thenReturn("bunny", "ben");
        when(resultSet.getString("password")).thenReturn("1234", "4321");
        when(resultSet.getString("email")).thenReturn("bunny@test.com", "ben@test.com");

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, testRowMapper(), 1L))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("queryForObject로 조회 시 조회된 데이터가 없다면 예외가 발생한다.")
    @Test
    void queryForObject_WhenFindZeroResult() throws SQLException {
        //  given
        resultSet = mock(ResultSet.class);
        String sql = "select * from users where id = ?";
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, testRowMapper(), 1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    private RowMapper<FakeUser> testRowMapper() {
        return (rs, rowNum) ->
                new FakeUser(rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email"));
    }
}
