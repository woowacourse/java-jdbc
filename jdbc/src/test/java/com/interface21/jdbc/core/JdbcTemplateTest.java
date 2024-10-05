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

class JdbcTemplateTest {
    private static final long TEST_ID = 1;

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement pstmt;

    @BeforeEach
    void setup() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection conn = mock(Connection.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        pstmt = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
    }

    record TestEntity(Long id, String attribute1, String attribute2, String attribute3) {
    }

    static class TestEntityRowMapper implements RowMapper<TestEntity> {
        @Override
        public TestEntity mapRow(ResultSet resultSet) throws SQLException {
            return new TestEntity(
                    resultSet.getLong("id"),
                    resultSet.getString("test_attribute1"),
                    resultSet.getString("test_attribute2"),
                    resultSet.getString("test_attribute3")
            );
        }
    }

    @DisplayName("JdbcTemplate이 query와 parameters로 executeUpdate를 실행할 수 있다.")
    @Test
    void testExecuteUpdate() throws SQLException {
        // given
        final var query = "update test_table set test_attribute1 = ?, test_attribute2 = ?, test_attribute3 = ?";
        Object[] parameters = {"test_value1", "test_value2", "test_value3"};

        // when
        jdbcTemplate.executeUpdate(query, parameters);

        // then
        verify(pstmt).setObject(1, "test_value1");
        verify(pstmt).setObject(2, "test_value2");
        verify(pstmt).setObject(3, "test_value3");
        verify(pstmt).executeUpdate();
    }

    @DisplayName("JdbcTemplate이 query와 rowMapper, parameters로 executeQuery를 실행할 수 있다.")
    @Test
    void testExecuteQuery() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(TEST_ID);
        when(resultSet.getString("test_attribute1")).thenReturn("test_value1");
        when(resultSet.getString("test_attribute2")).thenReturn("test_value2");
        when(resultSet.getString("test_attribute3")).thenReturn("test_value3");

        final var query = "select id, test_attribute1, test_attribute2, test_attribute3 from test_table where id = ?";
        Object[] parameters = {TEST_ID};

        // when
        List<TestEntity> results = jdbcTemplate.executeQuery(query, new TestEntityRowMapper(), parameters);

        // then
        verify(pstmt).setObject(1, TEST_ID);
        verify(pstmt).executeQuery();
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.getFirst().attribute1()).isEqualTo("test_value1");
        assertThat(results.getFirst().attribute2()).isEqualTo("test_value2");
        assertThat(results.getFirst().attribute3()).isEqualTo("test_value3");
    }
}
