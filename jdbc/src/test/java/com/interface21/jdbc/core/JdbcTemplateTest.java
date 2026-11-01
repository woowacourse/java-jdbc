package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "select name from users where id = ?";
    private static final RowMapper<String> ROW_MAPPER = rs -> rs.getString("name");

    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update는_파라미터를_바인딩하고_실행한_뒤_자원을_닫는다() throws SQLException {
        jdbcTemplate.update(SQL, "gugu", 1L);

        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, 1L);
        verify(pstmt).executeUpdate();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void update는_PreparedStatementSetter로_파라미터를_바인딩할_수_있다() throws SQLException {
        jdbcTemplate.update(SQL, pstmt -> pstmt.setLong(1, 1L));

        verify(pstmt).setLong(1, 1L);
        verify(pstmt).executeUpdate();
    }

    @Test
    void query는_PreparedStatementSetter로_파라미터를_바인딩할_수_있다() throws SQLException {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("name")).thenReturn("gugu");

        final var actual = jdbcTemplate.query(SQL, ROW_MAPPER, pstmt -> pstmt.setLong(1, 1L));

        assertThat(actual).containsExactly("gugu");
        verify(pstmt).setLong(1, 1L);
    }

    @Test
    void query는_모든_행을_매핑하고_자원을_닫는다() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("name")).thenReturn("gugu", "kukim");

        final var actual = jdbcTemplate.query(SQL, ROW_MAPPER, 1L);

        assertThat(actual).containsExactly("gugu", "kukim");
        verify(pstmt).setObject(1, 1L);
        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void queryForObject는_조회_결과가_있으면_매핑한_값을_반환한다() throws SQLException {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("name")).thenReturn("gugu");

        final var actual = jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L);

        assertThat(actual).contains("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void queryForObject는_조회_결과가_없으면_빈_값을_반환한다() throws SQLException {
        when(rs.next()).thenReturn(false);

        final var actual = jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L);

        assertThat(actual).isEmpty();
    }

    @Test
    void queryForObject는_조회_결과가_2건_이상이면_예외가_발생한다() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("name")).thenReturn("gugu", "kukim");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void SQLException이_발생하면_DataAccessException으로_변환하고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("실행 실패");
        when(pstmt.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void RowMapper에서_SQLException이_발생해도_DataAccessException으로_변환하고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("매핑 실패");
        when(rs.next()).thenReturn(true);
        when(rs.getString("name")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }
}
