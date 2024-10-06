package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    @DisplayName("정상적으로 SQL을 실행하고 업데이트 수를 반환한다.")
    void update() throws SQLException {
        String sql = "INSERT INTO test (id, name) VALUES (?, ?)";
        Object[] params = {1, "test"};

        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertAll(
                () -> assertThat(jdbcTemplate.update(sql, params)).isEqualTo(1),
                () -> verify(preparedStatement).setObject(1, 1),
                () -> verify(preparedStatement).setObject(2, "test")
        );
    }

    @Test
    @DisplayName("단 건 데이터를 조회한다.")
    void queryForObject() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("test");

        RowMapper<TestObject> rowMapper = rs -> new TestObject(
                rs.getInt("id"),
                rs.getString("name")
        );

        String sql = "SELECT * FROM test WHERE id = ?";

        assertAll(
                () -> assertThat(jdbcTemplate.queryForObject(sql, rowMapper, 1)).isEqualTo(new TestObject(1, "test")),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("여러 건의 데이터를 조회한다.")
    void query() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("test1", "test2");

        RowMapper<TestObject> rowMapper = rs -> new TestObject(
                rs.getInt("id"),
                rs.getString("name")
        );

        String sql = "SELECT * FROM test";

        assertAll(
                () -> assertThat(jdbcTemplate.query(sql, rowMapper, 1))
                        .containsExactly(new TestObject(1, "test1"), new TestObject(2, "test2")),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("단 건 조회 시, 데이터가 없으면 예외가 발생한다.")
    void queryForObjectWhenNotFound() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        RowMapper<TestObject> rowMapper = rs -> new TestObject(
                rs.getInt("id"),
                rs.getString("name")
        );

        String sql = "SELECT * FROM test WHERE id = ?";

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessageContaining("Expected a single result, but not found for query: "),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("단 건 조회 시, 데이터가 두 개 이상이면 예외가 발생한다.")
    void queryForObjectWhenMultipleData() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("test1", "test2");

        RowMapper<TestObject> rowMapper = rs -> new TestObject(
                rs.getInt("id"),
                rs.getString("name")
        );

        String sql = "SELECT * FROM test WHERE id = ?";

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessageContaining("Expected a single result, but found multiple for query: "),
                () -> verify(resultSet).close()
        );
    }

    private record TestObject(int id, String name) {
    }
}
