package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("단 건 정상 조회 테스트")
    void queryForObject() throws SQLException {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("column")).thenReturn("expected");

        String sql = "select * from users where id = ?";
        RowMapper<String> rowMapper = rs -> rs.getString("column");
        String result = jdbcTemplate.queryForObject(sql, rowMapper, 1);

        assertAll(
                () -> assertEquals("expected", result),
                () -> verify(this.rs).close(),
                () -> verify(this.pstmt).close(),
                () -> verify(this.connection).close()
        );
    }

    @Test
    @DisplayName("조회된 결과가 없을 경우 예외 처리 테스트")
    void queryForObjectWhenResultIsEmpty() throws SQLException {
        when(rs.next()).thenReturn(false);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                        "select * from users where id=?",
                        rs -> rs.getString("column"),
                        1
                )).isInstanceOf(DataAccessException.class),
                () -> verify(this.rs).close(),
                () -> verify(this.pstmt).close(),
                () -> verify(this.connection).close()
        );
    }

    @Test
    @DisplayName("조회된 결과가 2개 이상일 경우 예외 처리 테스트")
    void queryForObjectWhenResultIsMoreThanTwo() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                        "select * from users where id=?",
                        rs -> rs.getString("column"),
                        1
                )).isInstanceOf(DataAccessException.class),
                () -> verify(this.rs).close(),
                () -> verify(this.pstmt).close(),
                () -> verify(this.connection).close()
        );
    }

    @Test
    @DisplayName("여러 건 정상 조회 테스트")
    void query() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("column")).thenReturn("value1", "value2");

        String sql = "select * from users";
        RowMapper<String> rowMapper = rs -> rs.getString("column");
        List<String> result = jdbcTemplate.query(sql, rowMapper);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("value1", result.get(0)),
                () -> assertEquals("value2", result.get(1)),
                () -> verify(this.rs).close(),
                () -> verify(this.pstmt).close(),
                () -> verify(this.connection).close()
        );
    }

    @Test
    @DisplayName("업데이트 테스트")
    void update() throws SQLException {
        when(pstmt.executeUpdate()).thenReturn(1);

        String sql = "update users set column = ? where id = ?";
        Object[] parameters = {"value", 1};

        jdbcTemplate.update(sql, parameters);

        assertAll(
                () -> verify(pstmt, times(1)).executeUpdate(),
                () -> verify(pstmt).setObject(1, "value"),
                () -> verify(pstmt).setObject(2, 1),
                () -> verify(this.pstmt).close(),
                () -> verify(this.connection).close()
        );
    }
}
