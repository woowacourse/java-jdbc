package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.exception.JdbcQueryException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;
    private RowMapper<TestObject> testObjectRowMapper;

    private record TestObject(Long id, String name) {
    }

    @BeforeEach
    void setUp() throws SQLException {
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
        testObjectRowMapper = resultSet -> new TestObject(
                resultSet.getLong("id"),
                resultSet.getString("name")
        );
    }

    @Test
    @DisplayName("executeUpdate 시 PreparedStatement에 값을 저장한다.")
    void should_return_affected_row_count_when_executeUpdate() throws SQLException {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        // when
        jdbcTemplate.executeUpdate(sql, "account", "password", "asdf@gmail.com");

        // then
        verify(preparedStatement).setObject(1, "account");
        verify(preparedStatement).setObject(2, "password");
        verify(preparedStatement).setObject(3, "asdf@gmail.com");
        verify(preparedStatement, atLeastOnce()).executeUpdate();
    }

    @Test
    @DisplayName("executeUpdate 시 SQLException이 발생한다면 JdbcQueryException을 발생한다.")
    void should_throw_JdbcQueryException_when_executeUpdate_SQLException_thrown() throws SQLException {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        DataSource wrongDataSource = mock(DataSource.class);
        Connection wrongConnection = mock(Connection.class);
        when(wrongDataSource.getConnection()).thenReturn(wrongConnection);
        when(wrongConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        JdbcTemplate wrongJdbcTemplate = new JdbcTemplate(wrongDataSource);

        // when & then
        assertThatThrownBy(() -> wrongJdbcTemplate.executeUpdate(sql, "account", "password", "asdf@gmail.com"))
                .isInstanceOf(JdbcQueryException.class);
    }

    @Test
    @DisplayName("query 시 조회된 데이터가 있으면 List로 반환한다.")
    void should_return_List_when_query_multiple_data_found() throws SQLException {
        // given
        String sql = "select * from test_object";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("name1", "name2");

        // when
        List<TestObject> testObjects = jdbcTemplate.query(sql, testObjectRowMapper);

        // then
        assertAll(
                () -> assertThat(testObjects)
                        .containsExactly(new TestObject(1L, "name1"), new TestObject(2L, "name2")),
                () -> assertThat(testObjects)
                        .isInstanceOf(List.class)
        );
    }

    @Test
    @DisplayName("query 시 조회된 데이터가 없으면 빈 List를 반환한다.")
    void should_return_empty_List_when_query_data_not_found() throws SQLException {
        // given
        String sql = "select * from test_object";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // when
        List<TestObject> testUsers = jdbcTemplate.query(sql, testObjectRowMapper);

        // then
        assertThat(testUsers).isEmpty();
    }

    @Test
    @DisplayName("query 시 SQLException이 발생한다면 JdbcQueryException을 발생한다.")
    void should_throw_JdbcQueryException_when_query_SQLException_thrown() throws SQLException {
        // given
        String sql = "select * from users";

        DataSource wrongDataSource = mock(DataSource.class);
        Connection wrongConnection = mock(Connection.class);
        when(wrongDataSource.getConnection()).thenReturn(wrongConnection);
        when(wrongConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        JdbcTemplate wrongJdbcTemplate = new JdbcTemplate(wrongDataSource);

        // when & then
        assertThatThrownBy(() -> wrongJdbcTemplate.executeUpdate(sql))
                .isInstanceOf(JdbcQueryException.class);
    }

    @Test
    @DisplayName("queryForObject 시 조회된 데이터가 있으면 데이터가 들어있는 Optional을 반환한다.")
    void should_return_Optional_with_data_when_queryForObject_single_data_found() throws SQLException {
        // given
        String sql = "select id, name from test_object where id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("name1");

        // when
        Optional<TestObject> testObject = jdbcTemplate.queryForObject(sql, testObjectRowMapper, 1L);

        // then
        assertAll(
                () -> assertThat(testObject.isPresent()).isTrue(),
                () -> assertThat(testObject.get().id).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("queryForObject 시 조회된 데이터가 있으면 데이터가 들어있는 Optional을 반환한다.")
    void should_return_Optional_without_data_when_queryForObject_data_not_found() throws SQLException {
        // given
        String sql = "select id, name from test_object where id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // when
        Optional<TestObject> testObject = jdbcTemplate.queryForObject(sql, testObjectRowMapper, 1L);

        // then
        assertThat(testObject).isEmpty();
    }
}
