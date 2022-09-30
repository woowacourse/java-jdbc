package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import nextstep.jdbc.fixture.Tester;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SingleResultSetExtractorTest {

    @Test
    @DisplayName("ResultSet을 입력받아 RowMapper에 정의된 콜백에 따라 객체를 반환한다")
    void testExtractDataSucceeds() throws SQLException {
        // given
        final var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getString(2)).thenReturn("awesomeo");

        RowMapper<Tester> rowMapper = (rs, rowNum) -> new Tester(rs.getLong(1), rs.getString(2));
        final var singleResultSetExtractor = new SingleResultSetExtractor<>(rowMapper);

        // when
        final var actual = singleResultSetExtractor.extractData(resultSet).get();

        // then
        var expected = new Tester(1L, "awesomeo");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("ResultSet이 비어있으면 Empty Optional을 반환한다")
    void testExtractDataWithEmptyResult() throws SQLException {
        // given
        final var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);

        RowMapper<Tester> rowMapper = (rs, rowNum) -> null;
        final var singleResultSetExtractor = new SingleResultSetExtractor<>(rowMapper);

        // when
        final var actual = singleResultSetExtractor.extractData(resultSet);

        // then
        assertThat(actual).isEmpty();
    }
}
