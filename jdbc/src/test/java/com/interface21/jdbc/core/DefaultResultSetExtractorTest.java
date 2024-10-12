package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class DefaultResultSetExtractorTest {

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void init() {
        resultSet = mock(ResultSet.class);
    }

    @DisplayName("RowMapper로 결과를 매핑해 List를 반환한다.")
    @Test
    void extractResults() throws SQLException {
        RowMapper<String> USER_ROW_MAPPER = resultSet -> "kaki";
        DefaultResultSetExtractor<String> defaultResultSetExtractor = new DefaultResultSetExtractor<>(USER_ROW_MAPPER);
        when(resultSet.next()).thenReturn(true, false);

        List<String> results = defaultResultSetExtractor.extractResults(resultSet);

        assertAll(
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.getFirst()).isEqualTo("kaki"),
                () -> verify(resultSet, times(2)).next()
        );
    }
}
