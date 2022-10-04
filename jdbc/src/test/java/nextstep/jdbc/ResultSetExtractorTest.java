package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

class ResultSetExtractorTest {

    @Test
    @DisplayName("해당 데이터가 0개일때 에러를 발생시킨다.")
    void extractErrorWhenNoData() throws SQLException {
        //given, when
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper rowMapper = mock(RowMapper.class);

        when(resultSet.getRow()).thenReturn(0);

        //then
        assertThatThrownBy(() -> ResultSetExtractor.extract(rowMapper, resultSet))
            .isInstanceOf(EmptyResultDataAccessException.class)
            .hasMessage("데이터가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("해당 데이터가 2개 이상일때 에러를 발생시킨다.")
    void extractErrorWhenMoreThanTwoData() throws SQLException {
        //given, when
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper rowMapper = mock(RowMapper.class);

        when(resultSet.getRow()).thenReturn(2);

        //then
        assertThatThrownBy(() -> ResultSetExtractor.extract(rowMapper, resultSet))
            .isInstanceOf(IncorrectResultSizeDataAccessException.class)
            .hasMessage("데이터의 크기가 적절하지 않습니다.");
    }
}