package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.exception.ResultSetMappingFailureException;
import nextstep.jdbc.utils.MockResultSet;
import nextstep.jdbc.utils.MockResultSet.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultSetExtractorTest {

    @DisplayName("ResultSet으로부터 결과를 List 자료구조로 추출한다")
    @Test
    void toList() throws SQLException {
        // given
        int resultSetSize = 3;
        ResultSet resultSet = MockResultSet.ofSample(resultSetSize);
        RowMapper<Sample> rowMapper = MockResultSet.sampleRowMapper();
        ResultSetExtractor<Sample> resultSetExtractor = new ResultSetExtractor<>(resultSet, rowMapper);

        // when
        List<Sample> actual = resultSetExtractor.toList();

        // then
        assertThat(actual).hasSize(3);
    }

    @DisplayName("ResultSet으로부터 결과를 List 자료구조로 추출한다 - 실패, ResultSet 닫힘")
    @Test
    void toList_withClosedResultSet() throws SQLException {
        // given
        int resultSetSize = 3;
        ResultSet resultSet = MockResultSet.ofSample(resultSetSize);
        RowMapper<Sample> rowMapper = MockResultSet.sampleRowMapper();
        ResultSetExtractor<Sample> resultSetExtractor = new ResultSetExtractor<>(resultSet, rowMapper);

        // when // then
        resultSet.close();
        assertThatThrownBy(resultSetExtractor::toList)
            .isExactlyInstanceOf(ResultSetMappingFailureException.class);
    }
}
