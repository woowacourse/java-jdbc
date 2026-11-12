package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private static final RowMapper<String> NAME_ROW_MAPPER = (rs, rowNum) -> rs.getString("name");

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 가변인자를_순서대로_바인딩하고_변경된_행의_수를_반환한다() throws SQLException {
        given(pstmt.executeUpdate()).willReturn(1);

        final int updated = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        assertThat(updated).isEqualTo(1);
        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, 1L);
    }

    @Test
    void 트랜잭션에_바인딩된_Connection을_사용하고_닫지_않는다() throws SQLException {
        final Connection txConnection = mock(Connection.class);
        given(txConnection.prepareStatement(anyString())).willReturn(pstmt);
        TransactionSynchronizationManager.bindResource(dataSource, txConnection);

        try {
            jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }

        verify(txConnection).prepareStatement(anyString());
        verify(pstmt).close();
        verify(dataSource, never()).getConnection();
        verify(txConnection, never()).close();
    }

    @Test
    void PreparedStatementSetter로_값을_직접_바인딩할_수_있다() throws SQLException {
        jdbcTemplate.update("delete from users where id = ?", statement -> statement.setLong(1, 1L));

        verify(pstmt).setLong(1, 1L);
        verify(pstmt).executeUpdate();
    }

    @Test
    void 조회_결과를_RowMapper로_변환하고_행_번호를_0부터_전달한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);

        final List<Integer> rowNums = jdbcTemplate.query("select id from users", (resultSet, rowNum) -> rowNum);

        assertThat(rowNums).containsExactly(0, 1);
    }

    @Test
    void PreparedStatementSetter와_RowMapper를_함께_전달해_조회할_수_있다() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getString("name")).willReturn("gugu");

        final List<String> names = jdbcTemplate.query(
                "select name from users where id = ?",
                statement -> statement.setLong(1, 1L),
                NAME_ROW_MAPPER);

        assertThat(names).containsExactly("gugu");
        verify(pstmt).setLong(1, 1L);
    }

    @Test
    void 단건_조회_결과를_반환한다() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getString("name")).willReturn("gugu");

        final String name = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(name).isEqualTo("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
        given(rs.next()).willReturn(false);

        final String name = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(name).isNull();
    }

    @Test
    void 단건_조회_결과가_여러_개면_예외가_발생한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users", NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void SQLException은_DataAccessException으로_변환된다() throws SQLException {
        final SQLException cause = new SQLException("실패");
        given(pstmt.executeUpdate()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void 조회가_끝나면_모든_자원을_닫는다() throws SQLException {
        given(rs.next()).willReturn(false);

        jdbcTemplate.query("select name from users", NAME_ROW_MAPPER);

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void 예외가_발생해도_자원을_닫는다() throws SQLException {
        given(pstmt.executeUpdate()).willThrow(new SQLException("실패"));

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class);

        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void 실행과_Connection_정리가_함께_실패하면_실행_예외를_유지하고_정리_실패는_suppressed로_남긴다() throws SQLException {
        final SQLException executeFailure = new SQLException("실행 실패");
        final SQLException closeFailure = new SQLException("닫기 실패");
        given(pstmt.executeUpdate()).willThrow(executeFailure);
        willThrow(closeFailure).given(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(executeFailure)
                .satisfies(e -> assertThat(e.getSuppressed())
                        .singleElement()
                        .isInstanceOf(CannotGetJdbcConnectionException.class)
                        .extracting(Throwable::getCause)
                        .isSameAs(closeFailure));

        verify(connection).close();
    }

    @Test
    void 실행이_성공하고_Connection_정리만_실패하면_정리_예외가_발생한다() throws SQLException {
        willThrow(new SQLException("닫기 실패")).given(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(CannotGetJdbcConnectionException.class);
    }
}
