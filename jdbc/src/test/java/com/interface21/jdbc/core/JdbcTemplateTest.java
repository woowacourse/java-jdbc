package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    @DisplayName("DB에 쿼리하여 데이터를 업데이트한다.")
    void update_database_via_query() throws SQLException {
        // given
        String sql = "UPDATE users SET account = 'fram' WHERE id = 1";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql);

        // then
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("DB 조회 결과를 자바 객체로 불러온다.")
    void get_persistence_by_java_object() throws SQLException {
        // given
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true)
                .thenReturn(false);

        final RowMapStrategy<String> rowMapStrategy = mock(RowMapStrategy.class);
        when(rowMapStrategy.mapRow(resultSet)).thenReturn("테스트결과");
        final String sql = "SELECT id, account, password, email FORM users WHERE id = ?";

        // when
        final String result = jdbcTemplate.queryForObject(sql, rowMapStrategy, 1L);

        // then
        assertThat(result).isEqualTo("테스트결과");
    }

    @Test
    @DisplayName("여러건의 데이터 조회 시 List 형태로 불러온다.")
    void get_persistence_by_List_collection() throws SQLException {
        // given
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        final RowMapStrategy<String> rowMapStrategy = mock(RowMapStrategy.class);
        when(rowMapStrategy.mapRow(resultSet)).thenReturn("테스트결과");
        final String sql = "SELECT id, account, password, email FROM users";

        // when
        final List<String> result = jdbcTemplate.query(sql, rowMapStrategy);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst()).isEqualTo("테스트결과");
    }
}
