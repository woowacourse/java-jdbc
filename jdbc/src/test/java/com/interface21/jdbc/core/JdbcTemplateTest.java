package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @Test
    void 인자를_순서대로_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(connection, statement);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setObject(1, "password99");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 외부_연결로_수정하고_연결을_닫지_않는다() throws SQLException {
        final String sql = """
                update users
                set password = ?
                where id = ?
                """;
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(connection, sql, "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(connection, statement);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setObject(1, "password99");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void 외부_연결의_실행에_실패해도_구문만_닫고_원인_예외를_보존한다() throws SQLException {
        final String sql = """
                delete from users
                """;
        final var exception = new SQLException("쿼리 실행 실패");
        when(statement.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: " + sql)
                .hasCause(exception);

        verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void 외부_연결의_구문_준비에_실패해도_연결을_닫지_않는다() throws SQLException {
        final String sql = """
                delete from users
                """;
        final var exception = new SQLException("구문 준비 실패");
        when(connection.prepareStatement(sql)).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: " + sql)
                .hasCause(exception);

        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void 전달한_설정자로_인자를_설정하고_영향받은_행_수를_반환한다() throws SQLException {
        final String sql = """
                update users
                set password = ?
                where id = ?
                """;
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "password99");
            preparedStatement.setLong(2, 1L);
        });

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(connection, statement);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setString(1, "password99");
        order.verify(statement).setLong(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 단일_null_인자를_Object로_전달해_설정한다() throws SQLException {
        final String sql = "update sample_items set description = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, (Object) null);

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, null);
        order.verify(statement).executeUpdate();
    }

    @Test
    void 설정자의_setNull로_SQL_타입을_지정해_null을_설정한다() throws SQLException {
        final String sql = "update sample_items set description = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(
                sql,
                preparedStatement -> preparedStatement.setNull(1, Types.VARCHAR)
        );

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(statement);
        order.verify(statement).setNull(1, Types.VARCHAR);
        order.verify(statement).executeUpdate();
    }

    @Test
    void 설정자에서_SQL예외가_발생하면_실행하지_않고_자원을_닫는다() throws SQLException {
        final String sql = """
                update users
                set password = ?
                where id = ?
                """;
        final var exception = new SQLException("매개변수 설정 실패");

        assertThatThrownBy(() -> jdbcTemplate.update(sql, preparedStatement -> {
            throw exception;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: " + sql)
                .hasCause(exception);

        verify(statement, never()).executeUpdate();
        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 인자가_없는_SQL도_실행한다() throws SQLException {
        when(statement.executeUpdate()).thenReturn(3);

        final var affectedRows = jdbcTemplate.update("delete from users");

        assertThat(affectedRows).isEqualTo(3);
        verify(statement, never()).setObject(anyInt(), any());
        verify(statement).executeUpdate();
    }

    @Test
    void 실행에_실패하면_자원을_닫고_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("쿼리 실행 실패");
        when(statement.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: delete from users")
                .hasCause(exception);

        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 구문_준비에_실패해도_연결을_닫는다() throws SQLException {
        final var exception = new SQLException("구문 준비 실패");
        when(connection.prepareStatement(anyString())).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(connection).close();
    }

    @Test
    void 연결에_실패하면_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(connection, never()).prepareStatement(anyString());
    }

    @Test
    void 조회_인자를_바인딩하고_각_행을_변환해_반환한다() throws SQLException {
        final String sql = "select account from users where email = ? and id > ?";
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final var accounts = jdbcTemplate.query(
                sql,
                row -> row.getString("account"),
                "test@woowahan.com",
                0L
        );

        assertThat(accounts).containsExactly("gugu", "pobi");
        verify(connection).prepareStatement(sql);
        final var order = inOrder(statement, resultSet, connection);
        order.verify(statement).setObject(1, "test@woowahan.com");
        order.verify(statement).setObject(2, 0L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 조회_설정자로_인자를_설정하고_각_행을_변환해_반환한다() throws SQLException {
        final String sql = """
                select account
                from users
                where email = ? and id > ?
                """;
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final var accounts = jdbcTemplate.query(
                sql,
                row -> row.getString("account"),
                preparedStatement -> {
                    preparedStatement.setString(1, "test@woowahan.com");
                    preparedStatement.setLong(2, 0L);
                }
        );

        assertThat(accounts).containsExactly("gugu", "pobi");
        final var order = inOrder(connection, statement, resultSet);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setString(1, "test@woowahan.com");
        order.verify(statement).setLong(2, 0L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 조회_설정자에서_SQL예외가_발생하면_조회하지_않고_자원을_닫는다() throws SQLException {
        final String sql = """
                select account
                from users
                where id = ?
                """;
        final var exception = new SQLException("조회 매개변수 설정 실패");

        assertThatThrownBy(() -> jdbcTemplate.query(
                sql,
                row -> row.getString("account"),
                preparedStatement -> {
                    throw exception;
                }
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: " + sql)
                .hasCause(exception);

        verify(statement, never()).executeQuery();
        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 조회_결과가_없으면_빈_리스트를_반환한다() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final var accounts = jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        );

        assertThat(accounts).isEmpty();
        verify(resultSet, never()).getString(anyString());
    }

    @Test
    void 조회_실행에_실패하면_자원을_닫고_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("조회 실행 실패");
        when(statement.executeQuery()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: select account from users")
                .hasCause(exception);

        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 행_변환에_실패하면_모든_자원을_닫고_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("행 변환 실패");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        ))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 외부_연결로_단건_조회하고_결과와_구문만_닫는다() throws SQLException {
        final String sql = "select account from users where id = ? for update";
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(
                connection, sql, row -> row.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
        final var order = inOrder(connection, statement, resultSet);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setObject(1, 1L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void 외부_연결의_조회가_실패해도_연결은_닫지_않는다() throws SQLException {
        final String sql = "select account from users where id = ? for update";
        final var exception = new SQLException("조회 실패");
        when(statement.executeQuery()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                connection, sql, row -> row.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void 외부_연결의_행_변환이_실패해도_결과와_구문만_닫는다() throws SQLException {
        final String sql = "select account from users where id = ? for update";
        final var exception = new SQLException("행 변환 실패");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                connection, sql, row -> row.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        final var order = inOrder(resultSet, statement);
        order.verify(resultSet).close();
        order.verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void 단건_조회_결과가_한_건이면_객체를_반환한다() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                row -> row.getString("account"),
                1L
        );

        assertThat(account).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
    }

    @Test
    void 단건_조회_설정자로_인자를_설정하고_객체를_반환한다() throws SQLException {
        final String sql = """
                select account
                from users
                where id = ?
                """;
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(
                sql,
                row -> row.getString("account"),
                preparedStatement -> preparedStatement.setLong(1, 1L)
        );

        assertThat(account).isEqualTo("gugu");
        final var order = inOrder(connection, statement, resultSet);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 단건_조회_중_행_변환에_실패하면_모든_자원을_닫는다() throws SQLException {
        final String sql = """
                select account
                from users
                where id = ?
                """;
        final var exception = new SQLException("단건 조회 행 변환 실패");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                sql,
                row -> row.getString("account"),
                1L
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다: " + sql)
                .hasCause(exception);

        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                row -> row.getString("account"),
                1L
        );

        assertThat(account).isNull();
    }

    @Test
    void 단건_조회_결과가_여러_건이면_예외가_발생한다() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users",
                row -> row.getString("account")
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단건 조회 결과는 최대 1개여야 합니다. 2개 이상의 행이 조회되었습니다.");

        verify(resultSet, times(2)).next();
        verify(resultSet).getString("account");
        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 두_번째_행의_변환이_실패할_수_있어도_복수_결과_예외를_던진다() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account"))
                .thenReturn("gugu")
                .thenThrow(new SQLException("두 번째 행 변환 실패"));

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users",
                row -> row.getString("account")
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단건 조회 결과는 최대 1개여야 합니다. 2개 이상의 행이 조회되었습니다.")
                .hasNoCause();

        verify(resultSet).getString("account");
    }
}
