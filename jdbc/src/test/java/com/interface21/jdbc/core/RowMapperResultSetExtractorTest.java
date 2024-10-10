package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RowMapperResultSetExtractorTest {

    @Test
    @DisplayName("RowMapper를 통해 ResultSet에서 값을 추출한다.")
    void extractFromResultSet() throws SQLException {
        record Fruit(int id, String name) {}

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("apple");

        RowMapper<Fruit> rowMapper = rs -> new Fruit(rs.getInt("id"), rs.getString("name"));
        RowMapperResultSetExtractor<Fruit> extractor = new RowMapperResultSetExtractor<>(rowMapper);
        List<Fruit> fruits = extractor.extractData(resultSet);

        assertThat(fruits).containsExactly(new Fruit(1, "apple"));
    }

    @Test
    @DisplayName("RowMapper가 null인 경우 예외가 발생한다.")
    void exceptionOnNullRowMapper() {
        assertThatThrownBy(() -> new RowMapperResultSetExtractor<>(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("RowMapper should not be null.");
    }
}
