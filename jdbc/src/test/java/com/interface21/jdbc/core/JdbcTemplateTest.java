package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @Test
    void 전달받은_DB_연결로_수정하고_연결은_닫지_않는다() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        String sql = "update items set name = ? where id = ?";
        given(connection.prepareStatement(sql)).willReturn(statement);
        given(statement.executeUpdate()).willReturn(1);

        // when
        final int updatedCount = jdbcTemplate.update(connection, sql, "changed", 1);

        // then
        assertThat(updatedCount).isEqualTo(1);
        then(statement).should().setObject(1, "changed");
        then(statement).should().setObject(2, 1);
        then(statement).should().close();
        then(connection).should(never()).commit();
        then(connection).should(never()).rollback();
        then(connection).should(never()).close();
    }

    @Test
    void 전달받은_연결에서_SQL이_실패해도_연결을_닫거나_롤백하지_않는다() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException failure = new SQLException("update failed");
        given(connection.prepareStatement("sql")).willReturn(statement);
        given(statement.executeUpdate()).willThrow(failure);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update(connection, "sql"))
            .isInstanceOf(DataAccessException.class)
            .hasCause(failure);
        then(statement).should().close();
        then(connection).should(never()).commit();
        then(connection).should(never()).rollback();
        then(connection).should(never()).close();
    }

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("create table items (id int primary key, name varchar(100))");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("set DB_CLOSE_DELAY 0");
    }

    @Test
    void SQL에_필요한_값이_빠지면_원인을_보존한_예외를_던진다() {
        // when
        Throwable exception = catchThrowable(() -> jdbcTemplate.update("insert into items values (?, ?)", 1));

        // then
        assertThat(exception)
            .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void SQL에_필요한_개수보다_많은_값을_전달하면_예외를_던진다() {
        // when
        Throwable exception = catchThrowable(
            () -> jdbcTemplate.update("insert into items values (?, ?)", 1, "name", "extra")
        );

        // then
        assertThat(exception)
            .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void 조회_SQL_실행에_실패해도_SQL_실행_객체와_DB_연결을_닫는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException failure = new SQLException("query failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);
        when(statement.executeQuery()).thenThrow(failure);

        // when
        Throwable exception = catchThrowable(() -> new JdbcTemplate(dataSource).query("sql", row -> "value"));

        // then
        assertThat(exception)
            .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void 조회한_행을_객체로_바꾸다_런타임_예외가_발생해도_DB_연결을_닫는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        IllegalArgumentException failure = new IllegalArgumentException("invalid row");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // when
        Throwable exception = catchThrowable(
            () -> new JdbcTemplate(dataSource).query("sql", row -> { throw failure; })
        );

        // then
        assertThat(exception).isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void 여러_행을_조회하면_각_행을_객체로_바꿔_목록으로_반환한다() {
        // given
        jdbcTemplate.update("insert into items values (?, ?), (?, ?)", 1, "first", 2, "second");

        // then
        assertThat(jdbcTemplate.query("select name from items order by id", row -> row.getString("name")))
            .containsExactly("first", "second");
    }

    @Test
    void 조회된_행이_없으면_빈_목록을_반환한다() {
        assertThat(jdbcTemplate.query("select name from items", row -> row.getString(1))).isEmpty();
    }

    @Test
    void 단건_조회에서_결과가_없으면_값이_없음을_반환한다() {
        assertThat(jdbcTemplate.queryForObject("select name from items where id = ?", row -> row.getString(1), 1))
            .isEmpty();
    }

    @Test
    void 단건_조회에서_결과가_하나면_조회한_객체를_반환한다() {
        // given
        jdbcTemplate.update("insert into items values (?, ?)", 1, "first");

        // then
        assertThat(jdbcTemplate.queryForObject("select name from items where id = ?", row -> row.getString(1), 1))
            .contains("first");
    }

    @Test
    void 단건_조회에서_결과가_둘_이상이면_예외를_던진다() {
        // given
        jdbcTemplate.update("insert into items values (1, 'first'), (2, 'second')");

        // when
        Throwable exception = catchThrowable(() -> jdbcTemplate.queryForObject("select name from items", row -> row.getString(1)));

        // then
        assertThat(exception)
            .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void 데이터를_삽입하면_DB에_저장되고_저장한_행_수를_반환한다() {
        // when
        final int affectedRows = jdbcTemplate.update("insert into items values (?, ?)", 1, "saved");

        // then
        assertThat(affectedRows).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select name from items where id = 1", row -> row.getString(1)))
            .contains("saved");
    }

    @Test
    void 데이터를_수정하면_DB에_반영되고_수정한_행_수를_반환한다() {
        // given
        jdbcTemplate.update("insert into items values (1, 'before'), (2, 'other')");

        // when
        final int affectedRows = jdbcTemplate.update("update items set name = ? where id = ?", "after", 1);

        // then
        assertThat(affectedRows).isEqualTo(1);
        assertThat(jdbcTemplate.query("select name from items order by id", row -> row.getString(1)))
            .containsExactly("after", "other");
    }

    @Test
    void 데이터를_삭제하면_DB에서_사라지고_삭제한_행_수를_반환한다() {
        // given
        jdbcTemplate.update("insert into items values (1, 'first')");

        // when
        final int affectedRows = jdbcTemplate.update("delete from items where id = ?", 1);

        // then
        assertThat(affectedRows).isEqualTo(1);
        assertThat(jdbcTemplate.query("select id from items", row -> row.getInt(1))).isEmpty();
    }

    @Test
    void DB_연결에_실패하면_원인을_보존한_예외를_던진다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        SQLException failure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(failure);

        // when
        Throwable exception = catchThrowable(() -> new JdbcTemplate(dataSource).update("delete from items"));

        // then
        assertThat(exception)
            .isInstanceOf(DataAccessException.class).hasCause(failure);
    }

    @Test
    void SQL_실행에_실패하면_원인을_보존한_예외를_던진다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException failure = new SQLException("execution failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(failure);

        // when
        Throwable exception = catchThrowable(() -> new JdbcTemplate(dataSource).update("sql"));

        // then
        assertThat(exception)
            .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void 조회를_마치면_조회_결과와_SQL_실행_객체와_DB_연결을_닫는다() throws SQLException {
        verifyQueryResources(false);
    }

    @Test
    void 조회한_행을_객체로_바꾸다_실패해도_조회_결과와_SQL_실행_객체와_DB_연결을_닫는다() throws SQLException {
        verifyQueryResources(true);
    }

    private void verifyQueryResources(final boolean failMapping) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        JdbcTemplate template = new JdbcTemplate(dataSource);
        if (failMapping) {
            SQLException failure = new SQLException("mapping failed");
            assertThatThrownBy(() -> template.query("sql", row -> { throw failure; }))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        }
        if (!failMapping) {
            assertThat(template.query("sql", row -> "mapped")).containsExactly("mapped");
        }
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void SQL_실행_객체를_만들다_실패해도_이미_얻은_DB_연결을_닫는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        SQLException failure = new SQLException("prepare failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenThrow(failure);

        // when
        Throwable exception = catchThrowable(() -> new JdbcTemplate(dataSource).update("sql"));

        // then
        assertThat(exception)
            .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(connection).close();
    }

    @Test
    void 수정을_마치면_SQL_실행_객체와_DB_연결을_닫는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);

        // when
        new JdbcTemplate(dataSource).update("sql");

        // then
        verify(statement).close();
        verify(connection).close();
    }
}
