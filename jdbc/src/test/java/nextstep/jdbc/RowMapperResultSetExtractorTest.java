package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RowMapperResultSetExtractorTest {

    @Test
    @DisplayName("extractData 메서드는 ResultSet의 내용을 RowMapper 형식에 매핑하여 반환한다.")
    void extractData() throws SQLException {
        // given
        final RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("account");
        final ResultSetExtractor<List<String>> resultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
        final ResultSet resultSet = mock(ResultSet.class);

        given(resultSet.next())
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("account"))
                .willReturn("pepper");

        // when
        final List<String> result = resultSetExtractor.extractData(resultSet);

        // then
        assertThat(result).containsExactly("pepper");
    }
}
