package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultMapperTest {

    private static final RowMapper<TestClass> TEST_CLASS_ROW_MAPPER = (rs, rowNum) -> new TestClass(
            rs.getString("column1"),
            rs.getString("column2"));

    private ResultSet resultSet;
    private ResultMapper resultMapper;

    @BeforeEach
    void setUp() {
        resultSet = mock(ResultSet.class);
        resultMapper = new ResultMapper();
    }

    @Nested
    @DisplayName("ResultSet으로부터 매핑한 객체 조회")
    class FindResult {

        @Test
        @DisplayName("ResultSet으로부터 매핑한 객체 조회")
        void findResult() throws SQLException {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString("column1")).thenReturn("hi");
            when(resultSet.getString("column2")).thenReturn("hogi");

            Optional<TestClass> result = resultMapper.findResult(resultSet, TEST_CLASS_ROW_MAPPER);

            TestClass actual = result.get();
            assertThat(actual).isEqualTo(new TestClass("hi", "hogi"));
        }

        @Test
        @DisplayName("ResultSet으로부터 매핑한 객체 조회: ResultSet이 전부 비어있는 경우 빈 옵셔널 반환")
        void findResultWhenResultSetIsAllEmpty() throws SQLException {
            when(resultSet.next()).thenReturn(false);

            assertThat(resultMapper.findResult(resultSet, TEST_CLASS_ROW_MAPPER)).isEmpty();
        }
    }

    @Nested
    @DisplayName("ResultSet으로부터 매핑한 객체 리스트 조회")
    class GetResults {

        @Test
        @DisplayName("ResultSet으로부터 매핑한 객체 리스트 조회")
        void getResults() throws SQLException {
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("column1")).thenReturn("hi", "hello");
            when(resultSet.getString("column2")).thenReturn("hogi", "world");

            List<TestClass> results = resultMapper.getResults(resultSet, TEST_CLASS_ROW_MAPPER);

            assertThat(results)
                    .hasSize(2)
                    .containsExactly(
                            new TestClass("hi", "hogi"),
                            new TestClass("hello", "world")
                    );
        }

        @Test
        @DisplayName("ResultSet으로부터 매핑한 객체 리스트 조회: ResultSet이 비어있는 경우 빈 리스트 반환")
        void getResultsWhenResultSetIsEmpty() throws SQLException {
            when(resultSet.next()).thenReturn(false);

            List<TestClass> results = resultMapper.getResults(resultSet, TEST_CLASS_ROW_MAPPER);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    class GetResult {

        @Test
        @DisplayName("ResultSet으로부터 매핑한 객체 조회")
        void getResult() throws SQLException {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString("column1")).thenReturn("hi");
            when(resultSet.getString("column2")).thenReturn("hogi");

            TestClass actual = resultMapper.getResult(resultSet, TEST_CLASS_ROW_MAPPER, 1);
            assertThat(actual).isEqualTo(new TestClass("hi", "hogi"));
        }
    }

    private record TestClass(String str1, String str2) {
    }
}
