package nextstep.jdbc;

import static nextstep.jdbc.Fixture.카더가든;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResultExtractorTest {

    @Test
    void 결과_데이터를_엔티티로_변환해서_반환한다() throws SQLException {
        // given
        final var resultSet = mock(ResultSet.class);
        final var metaData = mock(ResultSetMetaData.class);
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getMetaData()).willReturn(metaData);
        given(metaData.getColumnCount()).willReturn(2);
        given(metaData.getColumnClassName(anyInt())).willReturn(Long.class.getName(), String.class.getName());
        given(resultSet.getObject(anyInt())).willReturn(1L, "차정원");

        // when
        final List<User> users = ResultExtractor.extractData(User.class, resultSet);

        // then
        assertThat(users).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(카더가든);
    }
}
