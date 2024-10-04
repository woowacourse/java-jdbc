package com.interface21.jdbc.core;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MappedResultSetTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private RowMapper<String> rowMapper;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @DisplayName("limitCount가 설정되지 않은 경우, 모든 요소를 반환한다.")
    @Test
    void createWithoutLimit() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.map(resultSet)).thenReturn("first", "second");

        List<String> results = MappedResultSet.create(rowMapper, preparedStatement)
                .getResults();

        assertThat(results).hasSize(2)
                .containsExactly("first", "second");
    }

    @DisplayName("limitCount 내에 포함되는 요소만 반환한다.")
    @Test
    void createWithLimit() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(rowMapper.map(resultSet)).thenReturn("first", "second", "third");

        List<String> results = MappedResultSet.create(rowMapper, preparedStatement, 2)
                .getResults();

        assertThat(results).hasSize(2)
                .containsExactly("first", "second");
    }

    @DisplayName("첫 번째 요소가 존재하지 않는 경우 getFirst에서 Optional.empty를 반환한다.")
    @Test
    void getFirstWhenResultsAreEmpty() {
        MappedResultSet<String> mappedResultSet = new MappedResultSet<>(List.of());
        Optional<String> firstResult = mappedResultSet.getFirst();

        assertThat(firstResult).isEmpty();
    }

    @DisplayName("첫 번째 요소가 존재하는 경우 getFirst에서 해당 요소를 반환한다.")
    @Test
    void getFirstWhenResultsAreNotEmpty() {
        MappedResultSet<String> mappedResultSet = new MappedResultSet<>(List.of("first", "second"));
        String firstResult = mappedResultSet.getFirst().orElseThrow();

        assertThat(firstResult).isEqualTo("first");
    }
}
