package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.Sample;

class JdbcTemplateTest {
    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        this.dataSource = mock(DataSource.class);
        this.conn = mock(Connection.class);
        this.pstmt = mock(PreparedStatement.class);
        this.rs = mock(ResultSet.class);
    }

    @DisplayName("update 에서 불려지는 메소드를 확인한다.")
    @Test
    void update() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(pstmt);

        jdbcTemplate.update("delete from users");

        verify(pstmt).executeUpdate();
        verify(conn).close();
        verify(pstmt).close();
    }

    @DisplayName("query 에서 불려지는 메소드를 확인한다.")
    @Test
    void query() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(1L);
        when(rs.getString(2)).thenReturn("better");
        when(rs.getString(3)).thenReturn("query-test");

        // when
        Sample actual = jdbcTemplate.query("select name, note from samples where id = ?",
                (rs) -> new Sample(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3)), 1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getName()).isEqualTo("better");
        assertThat(actual.getNote()).isEqualTo("query-test");
        verify(pstmt).executeQuery();
        verify(conn).close();
        verify(pstmt).close();
        verify(rs).close();
    }

    @DisplayName("queryForList 에서 불려지는 메소드를 확인한다.")
    @Test
    void queryForList() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        doReturn(true).doReturn(true).doReturn(false).when(rs).next();
        doReturn(1L).doReturn(2L).when(rs).getLong(1);
        doReturn("better").doReturn("getter").when(rs).getString(2);
        doReturn("query-test").doReturn("query-test").when(rs).getString(3);

        // when
        List<Sample> actual = jdbcTemplate.queryForList("select name, note from samples where id = ?",
                (rs) -> new Sample(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3)));
        List<String> sampleNames = actual.stream()
                .map(Sample::getName)
                .collect(Collectors.toList());

        // then
        assertThat(actual).isNotEmpty();
        assertThat(actual).hasSize(2);
        assertThat(sampleNames).contains("better", "getter");
        verify(pstmt).executeQuery();
        verify(conn).close();
        verify(pstmt).close();
        verify(rs).close();
    }

    @DisplayName("잘못된 sql 경우 예외가 발생한다.")
    @Test
    void updateException() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenThrow(SQLException.class);

        assertThatThrownBy(() ->jdbcTemplate.update("wrong sql"))
                .isInstanceOf(RuntimeException.class);

        verify(conn).close();
    }
}
