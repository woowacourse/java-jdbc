package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.fixture.Tester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultSetExtractorTest {

    @Test
    @DisplayName("ResultSet을 입력받아 RowMapper에 정의된 콜백에 따라 객체 리스트를 반환한다")
    void testExtractDataSucceeds() throws SQLException {
        // given
        final var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(1)).thenReturn(1L, 2L);
        when(resultSet.getString(2)).thenReturn("awesomeo", "panda");

        RowMapper<Tester> rowMapper = (rs, rowNum) -> new Tester(rs.getLong(1), rs.getString(2));
        final var multipleResultSetExtractor = new ResultSetExtractor<>(rowMapper);

        // when
        final var actual = multipleResultSetExtractor.extractData(resultSet);

        // then
        var expected = List.of(new Tester(1L, "awesomeo"), new Tester(2L, "panda"));
        assertThat(actual).isEqualTo(expected);
    }
}
