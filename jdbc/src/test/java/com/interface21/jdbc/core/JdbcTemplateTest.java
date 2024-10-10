package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        jdbcTemplate = new JdbcTemplate();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @DisplayName("단건 조회 결과가 있는 경우 결과를 포함하는 Optional을 반환")
    @Test
    void queryForObject() throws SQLException {
        //given
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        //when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(rowMapper.mapRow(resultSet)).thenReturn(new TestUser(1L));

        Optional<TestUser> testUser = jdbcTemplate.queryForObject(connection, "sql", rowMapper, 1);

        //then
        assertAll(
                () -> assertThat(testUser).isPresent(),
                () -> assertThat(testUser.get().id()).isEqualTo(1L)
        );
    }

    @DisplayName("단건 조회 결과가 없는 경우 빈 Optional을 반환")
    @Test
    void queryForObject_resultNotFound() throws SQLException {
        //given
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        //when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<TestUser> testUser = jdbcTemplate.queryForObject(connection, "sql", rowMapper, 1);

        //then
        assertThat(testUser).isEmpty();
    }

    @DisplayName("조회 결과가 있는 경우 결과를 포함하는 리스트 반환")
    @Test
    void query() throws SQLException {
        //given
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        //when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(new TestUser(1L), new TestUser(2L));

        List<TestUser> testUsers = jdbcTemplate.query(connection, "sql", rowMapper);

        //then
        assertAll(
                () -> assertThat(testUsers).hasSize(2),
                () -> assertThat(testUsers.get(0).id()).isEqualTo(1L),
                () -> assertThat(testUsers.get(1).id()).isEqualTo(2L)
        );
    }

    @DisplayName("조회 결과가 없는 경우 빈 리스트 반환")
    @Test
    void query_resultNotFound() throws SQLException {
        //given
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        //when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<TestUser> testUsers = jdbcTemplate.query(connection, "sql", rowMapper);

        //then
        assertThat(testUsers).isEmpty();
    }

    @DisplayName("update 시 preparedStatement에 값이 저장되고 executeUpdate 호출")
    @Test
    void update() throws SQLException {
        //given
        Object[] values = new Object[]{1L, "capy"};

        //when
        jdbcTemplate.update(connection, "sql", values);

        //then
        verify(preparedStatement).setObject(1, values[0]);
        verify(preparedStatement).setObject(2, values[1]);
        verify(preparedStatement).executeUpdate();
    }

    private record TestUser(Long id) {
    }
}
