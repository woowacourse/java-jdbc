package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource = mock();

    private Connection connection = mock();

    private PreparedStatement preparedStatement = mock();

    private ResultSet resultSet = mock();

    private JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("queryForObject 단건 조회 성공")
    void queryForObject_success() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("name")).willReturn("test");

        // when
        final String name = jdbcTemplate.queryForObject(sql, rowMapper, 1L);

        // then
        assertThat(name).isEqualTo("test");

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("queryForObject 결과가 없으면 null 반환")
    void queryForObject_noResult() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

        // when
        final String name = jdbcTemplate.queryForObject(sql, rowMapper, 1L);

        // then
        assertThat(name).isNull();

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("queryForObject 결과가 2개 이상이면 예외 발생")
    void queryForObject_multipleResults() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("name"))
                .willReturn("test1")
                .willReturn("test2");

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1L))
                .isInstanceOf(com.interface21.dao.IncorrectResultSizeException.class)
                .hasMessageContaining("조회 결과 수가 2개 이상입니다");

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("queryForObject SQLException 발생 시 DataAccessException으로 변환")
    void queryForObject_sqlException() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willThrow(new SQLException("SQL Error"));

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("query 여러 행 조회 성공")
    void query_success() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE is_alive = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("name"))
                .willReturn("test1")
                .willReturn("test2");

        // when
        final List<String> names = jdbcTemplate.query(sql, rowMapper, true);

        // then
        assertThat(names).containsExactlyInAnyOrderElementsOf(List.of("test1", "test2"));

        verify(preparedStatement).setBoolean(1, true);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("query 결과가 없으면 빈 리스트 반환")
    void query_emptyResult() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE is_alive = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

        // when
        final List<String> names = jdbcTemplate.query(sql, rowMapper, true);

        // then
        assertThat(names).isEmpty();

        verify(preparedStatement).setBoolean(1, true);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("query SQLException 발생 시 DataAccessException으로 변환")
    void query_sqlException() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");

        given(preparedStatement.executeQuery()).willThrow(new SQLException("SQL Error"));

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.query(sql, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단일 파라미터로 update 성공")
    void update_success() throws Exception {
        // given
        final String sql = "INSERT INTO users (name) VALUES (?)";

        given(preparedStatement.executeUpdate()).willReturn(1);

        // when
        jdbcTemplate.update(sql, "test1");

        // then
        verify(preparedStatement).setString(1, "test1");
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("여러 파라미터로 update 성공")
    void update_withMultipleParameters() throws Exception {
        // given
        final String sql = "INSERT INTO users (name, is_alive) VALUES (?, ?)";

        given(preparedStatement.executeUpdate()).willReturn(1);

        // when
        jdbcTemplate.update(sql, "test1", true);

        // then
        verify(preparedStatement).setString(1, "test1");
        verify(preparedStatement).setBoolean(2, true);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("update SQLException 발생 시 DataAccessException으로 변환")
    void update_sqlException() throws Exception {
        // given
        final String sql = "INSERT INTO users (name, is_alive) VALUES (?)";

        given(preparedStatement.executeUpdate()).willThrow(new SQLException("SQL Error"));

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "test"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(preparedStatement).setString(1, "test");
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("Connection 획득 실패 시 DataAccessException으로 변환")
    void connectionFailure() throws Exception {
        // given
        final String sql = "INSERT INTO users (name) VALUES (?)";

        DataSource failingDataSource = mock(DataSource.class);
        Connection failingConnection = mock(Connection.class);

        given(failingDataSource.getConnection()).willReturn(failingConnection);
        given(failingConnection.prepareStatement(sql)).willThrow(new SQLException("Connection Error"));

        final JdbcTemplate failingJdbcTemplate = new JdbcTemplate(failingDataSource);

        // when & then
        assertThatThrownBy(() -> failingJdbcTemplate.update(sql, "test"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class)
                .hasMessageContaining("Connection Error");
    }

    @Test
    @DisplayName("PreparedStatement 생성 실패 시 DataAccessException으로 변환")
    void prepareStatementFailure() throws Exception {
        // given
        final String sql = "INVALID SQL";

        DataSource failingDataSource = mock(DataSource.class);
        Connection failingConnection = mock(Connection.class);

        given(failingDataSource.getConnection()).willReturn(failingConnection);
        given(failingConnection.prepareStatement(sql)).willThrow(new SQLException("SQL Syntax Error"));

        final JdbcTemplate failingJdbcTemplate = new JdbcTemplate(failingDataSource);

        // when & then
        assertThatThrownBy(() -> failingJdbcTemplate.update(sql, rs -> rs.setString(1, "test")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class)
                .hasMessageContaining("SQL Syntax Error");

        verify(failingConnection).close();
    }

    @Test
    @DisplayName("PreparedStatementSetter로 update 성공")
    void update_withPreparedStatementSetter() throws Exception {
        // given
        final String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        final PreparedStatementSetter setter = ps -> {
            ps.setString(1, "test1");
            ps.setInt(2, 20);
        };

        given(preparedStatement.executeUpdate()).willReturn(1);

        // when
        jdbcTemplate.update(sql, setter);

        // then
        verify(preparedStatement).setString(1, "test1");
        verify(preparedStatement).setInt(2, 20);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("PreparedStatementSetter로 query 성공")
    void query_withPreparedStatementSetter() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE age > ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");
        final PreparedStatementSetter setter = ps -> ps.setInt(1, 18);

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("name"))
                .willReturn("test1")
                .willReturn("test2");

        // when
        final List<String> names = jdbcTemplate.query(sql, rowMapper, setter);

        // then
        assertThat(names).containsExactlyInAnyOrderElementsOf(List.of("test1", "test2"));

        verify(preparedStatement).setInt(1, 18);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("PreparedStatementSetter로 queryForObject 성공")
    void queryForObject_withPreparedStatementSetter() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");
        final PreparedStatementSetter setter = ps -> ps.setLong(1, 1L);

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("name")).willReturn("test");

        // when
        final String name = jdbcTemplate.queryForObject(sql, rowMapper, setter);

        // then
        assertThat(name).isEqualTo("test");

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("PreparedStatementSetter로 queryForObject 결과가 없으면 null 반환")
    void queryForObject_withPreparedStatementSetter_noResult() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");
        final PreparedStatementSetter setter = ps -> ps.setLong(1, 999L);

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

        // when
        final String name = jdbcTemplate.queryForObject(sql, rowMapper, setter);

        // then
        assertThat(name).isNull();

        verify(preparedStatement).setLong(1, 999L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("PreparedStatementSetter로 queryForObject 결과가 2개 이상이면 예외 발생")
    void queryForObject_withPreparedStatementSetter_multipleResults() throws Exception {
        // given
        final String sql = "SELECT name FROM users WHERE id = ?";
        final RowMapper<String> rowMapper = rs -> rs.getString("name");
        final PreparedStatementSetter setter = ps -> ps.setLong(1, 1L);

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("name"))
                .willReturn("test1")
                .willReturn("test2");

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, setter))
                .isInstanceOf(com.interface21.dao.IncorrectResultSizeException.class)
                .hasMessageContaining("조회 결과 수가 2개 이상입니다");

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
