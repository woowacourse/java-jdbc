package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void 커넥션과_PreparedStatement를_해제한다() throws SQLException {
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Nested
    class Update {

        @Test
        void 파라미터를_순서대로_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            final int actual = jdbcTemplate.update(
                    "update users set account = ? where id = ?", "handa", 1L);

            assertThat(actual).isEqualTo(1);
            verify(preparedStatement).setObject(1, "handa");
            verify(preparedStatement).setObject(2, 1L);
        }

        @Test
        void SQL_실행에_실패하면_원인_예외를_보존한_DataAccessException을_던진다() throws SQLException {
            final SQLException failure = new SQLException("update failed");
            when(preparedStatement.executeUpdate()).thenThrow(failure);

            assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "handa"))
                    .isInstanceOf(DataAccessException.class)
                    .hasCause(failure);
        }

        @Test
        void PreparedStatementSetter로_개발자가_직접_값을_설정할_수_있다() throws SQLException {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            jdbcTemplate.update("update users set account = ? where id = ?", preparedStatement -> {
                preparedStatement.setString(1, "handa");
                preparedStatement.setLong(2, 1L);
            });

            verify(preparedStatement).setString(1, "handa");
            verify(preparedStatement).setLong(2, 1L);
        }
    }

    @Nested
    class Query {

        @AfterEach
        void ResultSet을_해제한다() throws SQLException {
            verify(resultSet).close();
        }

        @Test
        void 모든_행을_매핑해_리스트로_반환한다() throws SQLException {
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("account")).thenReturn("handa", "gugu");

            final List<String> actual = jdbcTemplate.query(
                    "select account from users where email = ?",
                    row -> row.getString("account"),
                    "handa@woowa.com");

            assertThat(actual).containsExactly("handa", "gugu");
            verify(preparedStatement).setObject(1, "handa@woowa.com");
        }

        @Test
        void 결과가_없으면_빈_리스트를_반환한다() throws SQLException {
            when(resultSet.next()).thenReturn(false);

            final List<String> actual = jdbcTemplate.query(
                    "select account from users", row -> row.getString("account"));

            assertThat(actual).isEmpty();
        }

        @Test
        void PreparedStatementSetter로_조회_조건을_설정할_수_있다() throws SQLException {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("account")).thenReturn("handa");

            final List<String> actual = jdbcTemplate.query(
                    "select account from users where id = ?",
                    row -> row.getString("account"),
                    preparedStatement -> preparedStatement.setLong(1, 1L));

            assertThat(actual).containsExactly("handa");
            verify(preparedStatement).setLong(1, 1L);
        }
    }

    @Nested
    class QueryForObject {

        @AfterEach
        void ResultSet을_해제한다() throws SQLException {
            verify(resultSet).close();
        }


        @Test
        void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
            when(resultSet.next()).thenReturn(false);

            final String actual = jdbcTemplate.queryForObject(
                    "select account from users", row -> row.getString("account"));

            assertThat(actual).isNull();
            verify(resultSet, never()).getString(anyString());
        }

        @Test
        void 단건_조회_결과가_한_건이면_객체를_반환한다() throws SQLException {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("account")).thenReturn("handa");

            final String actual = jdbcTemplate.queryForObject(
                    "select account from users where id = ?", row -> row.getString("account"), 1L);

            assertThat(actual).isEqualTo("handa");
            verify(preparedStatement).setObject(1, 1L);
        }

        @Test
        void 두_번째_행이_있으면_나머지_행을_매핑하지_않고_예외를_던진다() throws SQLException {
            when(resultSet.next()).thenReturn(true, true, true, false);
            when(resultSet.getString("account")).thenReturn("handa");

            assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                    "select account from users", row -> row.getString("account")))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessage("조회 결과가 1건이어야 하지만 2건 이상입니다.");

            verify(resultSet, times(2)).next();
            verify(resultSet).getString("account");
        }

        @Test
        void 단건_조회_결과_매핑에_실패하면_원인_예외를_보존한다() throws SQLException {
            final SQLException failure = new SQLException("mapping failed");
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString("account")).thenThrow(failure);

            assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                    "select account from users", row -> row.getString("account")))
                    .isInstanceOf(DataAccessException.class)
                    .hasCause(failure);
        }
    }
}
