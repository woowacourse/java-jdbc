package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResultMapperTest {

    @DisplayName("여러 행 결과를 RowMapper로 매핑할 수 있다")
    @Test
    void multipleResultMapping() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn("Row1", "Row2");

        List<String> result = ResultMapper.multipleResultMapping(rowMapper, resultSet);

        assertAll(
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat("Row1").isEqualTo(result.get(0)),
                () -> assertThat("Row2").isEqualTo(result.get(1))
        );

        verify(resultSet, times(3)).next();
        verify(rowMapper, times(2)).mapRow(resultSet);
    }

    @DisplayName("단일 행 결과를 RowMapper로 매핑할 수 있다")
    @Test
    void singleResultMapping() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn("SingleRow");

        String result = ResultMapper.singleResultMapping(rowMapper, resultSet);

        assertThat("SingleRow").isEqualTo(result);

        verify(resultSet, times(2)).next();
        verify(rowMapper, times(1)).mapRow(resultSet);
    }

    @DisplayName("단일 행을 기대했으나 결과가 없을 때 DataAccessException을 발생시킨다")
    @Test
    void failSingleResultMappingWhenNoResults() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> ResultMapper.singleResultMapping(rowMapper, resultSet))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단일 행 조회를 기대했지만, 조회된 행이 없습니다.");

        verify(resultSet, times(1)).next();
        verify(rowMapper, never()).mapRow(resultSet);
    }

    @DisplayName("단일 행을 기대했으나 여러 행이 조회될 때 DataAccessException을 발생시킨다")
    @Test
    void failSingleResultMappingWhenMultipleResults() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, true);
        when(rowMapper.mapRow(resultSet)).thenReturn("SingleRow");

        assertThatThrownBy(() -> ResultMapper.singleResultMapping(rowMapper, resultSet))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단일 행 조회를 기대했지만, 여러 행이 조회되었습니다.");

        verify(resultSet, times(2)).next();
        verify(rowMapper, times(1)).mapRow(resultSet);
    }
}
