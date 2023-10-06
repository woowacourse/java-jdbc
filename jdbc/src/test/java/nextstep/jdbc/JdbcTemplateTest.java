package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private static final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("name")
    );

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement pstmt = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 업데이트_쿼리를_실행한다() throws SQLException {
        // given
        var sql = "update user set name = 'aaaa' where id = 1";

        // when
        jdbcTemplate.executeQuery(sql);

        // then
        verify(pstmt).executeUpdate();
    }

    @Test
    void 쿼리를_실행할_때_파라미터를_적용해_쿼리문을_완성한다() throws SQLException {
        // given
        var sql = "delete from user where id = ? and name = ?";
        var id = 1L;
        var name = "doggy";

        // when
        jdbcTemplate.executeQuery(sql, id, name);

        // then
        verify(pstmt).setObject(1, id);
        verify(pstmt).setObject(2, name);
        verify(pstmt).executeUpdate();
    }

    @Test
    void 단일_조회를_한다() throws SQLException {
        // given
        var sql = "select * from user where id = ?";
        var id = 1L;

        when(resultSet.next()).thenReturn(true, false);

        // when
        jdbcTemplate.queryForObject(sql, rowMapper, id);

        // then
        verify(pstmt).setObject(1, id);
        verify(pstmt).executeQuery();
    }

    @Test
    void 단일_조회시에_조회_결과가_1개가_아니면_예외가_발생한다() throws SQLException {
        // given
        var sql = "select * from user where name = ?";
        var name = "doggy";

        when(resultSet.next()).thenReturn(true, true, false);

        // then
        assertThrows(DataAccessException.class,
                () -> jdbcTemplate.queryForObject(sql, rowMapper, name));
    }

    @Test
    void 조회_쿼리를_날린다() throws SQLException {
        // given
        var sql = "select * from user";

        // when
        jdbcTemplate.query(sql, rowMapper);

        // then
        verify(pstmt, times(0)).setObject(anyInt(), anyInt());
        verify(pstmt).executeQuery();
        verify(resultSet).next();
    }

    private static class User {

        private Long id;
        private String name;

        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
