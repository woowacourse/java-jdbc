package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.interface21.jdbc.exception.UnexpectedResultSizeException;

class JdbcTemplateTest {
    private static final Long TEST_ID = 1L;

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement pstmt;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection conn = mock(Connection.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        pstmt = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
    }

    record TestEntity(Long id, String attribute1, String attribute2, String attribute3) {
    }

    @DisplayName("JdbcTemplate이 query와 parameters로 executeUpdate를 실행할 수 있다.")
    @Test
    void testUpdate() throws SQLException {
        // given
        final var query = "update test_table set test_attribute1 = ?, test_attribute2 = ?, test_attribute3 = ?";

        // when
        jdbcTemplate.update(query, "test_value1", "test_value2", "test_value3");

        // then
        assertAll(
                () -> verify(pstmt).setObject(1, "test_value1"),
                () -> verify(pstmt).setObject(2, "test_value2"),
                () -> verify(pstmt).setObject(3, "test_value3"),
                () -> verify(pstmt).executeUpdate()
        );
    }

    @DisplayName("JdbcTemplate이 query와 rowMapper, parameters로 executeQueryForObject를 실행할 수 있다.")
    @Test
    void testQueryForObject() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(TEST_ID);
        when(resultSet.getString("test_attribute1")).thenReturn("test_value1");
        when(resultSet.getString("test_attribute2")).thenReturn("test_value2");
        when(resultSet.getString("test_attribute3")).thenReturn("test_value3");

        final var query = "select id, test_attribute1, test_attribute2, test_attribute3 from test_table where id = ?";

        // when
        Optional<TestEntity> optionalResult = jdbcTemplate.queryForObject(query, this::mapTestEntityFromResultSet, TEST_ID);
        assert(optionalResult.isPresent());
        TestEntity result = optionalResult.get();

        // then
        assertAll(
                () -> verify(pstmt).setObject(1, TEST_ID),
                () -> verify(pstmt).executeQuery(),
                () -> assertThat(result.attribute1()).isEqualTo("test_value1"),
                () -> assertThat(result.attribute2()).isEqualTo("test_value2"),
                () -> assertThat(result.attribute3()).isEqualTo("test_value3")
        );
    }

    @DisplayName("executeQueryForObject의 결과가 없다면, Optional.empty()를 반환한다.")
    @Test
    void testQueryForObject_ReturnOptionalEmpty_WhenResultSizeZero() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final var query = "select id, test_attribute1, test_attribute2, test_attribute3 from test_table where id = ?";

        // when
        Optional<TestEntity> optionalResult = jdbcTemplate.queryForObject(query, this::mapTestEntityFromResultSet, TEST_ID);

        // then
        assertThat(optionalResult).isEqualTo(Optional.empty());
    }

    @DisplayName("executeQueryForObject의 결과가 두 개 이상이면, 에러가 발생한다.")
    @Test
    void testQueryForObject_ThrowError_WhenResultSizeOverTwo() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        final var query = "select id, test_attribute1, test_attribute2, test_attribute3 from test_table where id = ?";

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(query, this::mapTestEntityFromResultSet, TEST_ID))
                .isInstanceOf(UnexpectedResultSizeException.class)
                .hasMessage("Multiple results returned for query, but only one result expected.");
    }

    @DisplayName("JdbcTemplate이 query와 rowMapper, parameters로 executeQuery를 실행할 수 있다.")
    @Test
    void testQuery() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        // when
        final String query = "select * from test_table";
        List<TestEntity> results = jdbcTemplate.query(query, this::mapTestEntityFromResultSet);

        // then
        assertAll(
                () -> verify(pstmt).executeQuery(),
                () -> assertThat(results.size()).isEqualTo(2)
        );
    }

    private TestEntity mapTestEntityFromResultSet(final ResultSet resultSet) throws SQLException {
        return new TestEntity(
                resultSet.getLong("id"),
                resultSet.getString("test_attribute1"),
                resultSet.getString("test_attribute2"),
                resultSet.getString("test_attribute3")
        );
    }
}
