package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    @DisplayName("update 메서드")
    class Update {

        private static final String SQL = "update users set password = ? where id = ?";

        @Test
        @DisplayName("인자를 바인딩해 쿼리를 실행한다.")
        void bindArgsAndReturnRowCount() throws SQLException {
            when(pstmt.executeUpdate()).thenReturn(1);

            final var rowCount = jdbcTemplate.update(SQL, "password99", 1L);

            verify(pstmt).setObject(1, "password99");
            verify(pstmt).setObject(2, 1L);
            assertThat(rowCount).isEqualTo(1);
        }

        @Test
        @DisplayName("update 후 자원을 반납한다.")
        void closeResources() throws SQLException {
            jdbcTemplate.update(SQL, "password99", 1L);

            verify(pstmt).close();
            verify(conn).close();
        }

        @Test
        @DisplayName("쿼리 실패 시 DataAccessException이 발생한다.")
        void throwDataAccessException() throws SQLException {
            when(pstmt.executeUpdate()).thenThrow(new SQLException());

            assertThatThrownBy(() -> jdbcTemplate.update(SQL, "password99", 1L))
                    .isInstanceOf(DataAccessException.class);
        }
    }

    @Nested
    @DisplayName("query 메서드")
    class Query {

        private static final String SQL = "select account from users where email = ?";
        private static final RowMapper<String> ROW_MAPPER = (rs, rowNum) -> rs.getString("account");

        private ResultSet rs;

        @BeforeEach
        void setUp() throws SQLException {
            rs = mock(ResultSet.class);
            when(pstmt.executeQuery()).thenReturn(rs);
        }

        @Test
        @DisplayName("인자를 바인딩해 조회 결과를 RowMapper로 변환한다.")
        void bindArgsAndMapRows() throws SQLException {
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("account")).thenReturn("gugu", "brown");

            final var accounts = jdbcTemplate.query(SQL, ROW_MAPPER, "hkkang@woowahan.com");

            verify(pstmt).setObject(1, "hkkang@woowahan.com");
            assertThat(accounts).containsExactly("gugu", "brown");
        }

        @Test
        @DisplayName("query 후 자원을 반납한다.")
        void closeResources() throws SQLException {
            jdbcTemplate.query(SQL, ROW_MAPPER, "hkkang@woowahan.com");

            verify(rs).close();
            verify(pstmt).close();
            verify(conn).close();
        }

        @Test
        @DisplayName("쿼리 실패 시 DataAccessException이 발생한다.")
        void throwDataAccessException() throws SQLException {
            when(pstmt.executeQuery()).thenThrow(new SQLException());

            assertThatThrownBy(() -> jdbcTemplate.query(SQL, ROW_MAPPER, "hkkang@woowahan.com"))
                    .isInstanceOf(DataAccessException.class);
        }
    }

    @Nested
    @DisplayName("queryForObject 메서드")
    class QueryForObject {

        private static final String SQL = "select account from users where id = ?";
        private static final RowMapper<String> ROW_MAPPER = (rs, rowNum) -> rs.getString("account");

        private ResultSet rs;

        @BeforeEach
        void setUp() throws SQLException {
            rs = mock(ResultSet.class);
            when(pstmt.executeQuery()).thenReturn(rs);
        }

        @Test
        @DisplayName("인자를 바인딩해 조회 결과 한 건을 RowMapper로 변환한다.")
        void bindArgsAndMapRow() throws SQLException {
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("account")).thenReturn("gugu");

            final var account = jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L);

            verify(pstmt).setObject(1, 1L);
            assertThat(account).isEqualTo("gugu");
        }

        @Test
        @DisplayName("조회 결과가 없으면 null을 반환한다.")
        void returnNullWhenEmpty() throws SQLException {
            when(rs.next()).thenReturn(false);

            final var account = jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L);

            assertThat(account).isNull();
        }

        @Test
        @DisplayName("조회 결과가 두 건 이상이면 DataAccessException이 발생한다.")
        void throwDataAccessExceptionWhenMultiple() throws SQLException {
            when(rs.next()).thenReturn(true, true, false);

            assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L))
                    .isInstanceOf(DataAccessException.class);
        }

        @Test
        @DisplayName("queryForObject 후 자원을 반납한다.")
        void closeResources() throws SQLException {
            when(rs.next()).thenReturn(true, false);

            jdbcTemplate.queryForObject(SQL, ROW_MAPPER, 1L);

            verify(rs).close();
            verify(pstmt).close();
            verify(conn).close();
        }
    }
}
