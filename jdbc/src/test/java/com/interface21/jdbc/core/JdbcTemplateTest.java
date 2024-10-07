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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    DataSource dataSource = mock(DataSource.class);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
    }

    @DisplayName("queryForObject는 커넥션을 가져오고 쿼리문을 실행한 뒤 결과를 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        String sql = "select * from mock_user where id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getString(2)).thenReturn("potato");

        RowMapper<MockUser> mockUserRowMapper = getMockUserRowMapper();
        MockUser mockUser = jdbcTemplate.queryForObject(getCreator(sql), mockUserRowMapper);

        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        assertThat(mockUser.getId()).isEqualTo(1L);
        assertThat(mockUser.getName()).isEqualTo("potato");
    }

    @DisplayName("query는 커넥션을 가져오고 쿼리문을 실행한 뒤 결과를 List로 반환한다.")
    @Test
    void query() throws SQLException {
        String sql = "select * from mock_user";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getLong(1)).thenReturn(1L, 2L, 3L);
        when(resultSet.getString(2)).thenReturn("potato", "tomato", "banana");

        RowMapper<MockUser> mockUserRowMapper = getMockUserRowMapper();
        List<MockUser> mockUsers = jdbcTemplate.query(sql, mockUserRowMapper);

        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        assertThat(mockUsers).hasSize(3);
        assertThat(mockUsers).contains(
                new MockUser(1L, "potato"),
                new MockUser(2L, "tomato"),
                new MockUser(3L, "banana")
        );
    }

    @DisplayName("update는 커넥션을 가져오고 쿼리문을 실행한다.")
    @Test
    void update() throws SQLException {
        String sql = "insert into mock_user (name) values (?)";

        jdbcTemplate.update(getCreator(sql));

        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeUpdate();
    }

    private static PreparedStatementCreator getCreator(String sql) {
        return connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, 1);
            return ps;
        };
    }

    private static RowMapper<MockUser> getMockUserRowMapper() {
        return rs -> new MockUser(
                rs.getLong(1),
                rs.getString(2)
        );
    }
}
