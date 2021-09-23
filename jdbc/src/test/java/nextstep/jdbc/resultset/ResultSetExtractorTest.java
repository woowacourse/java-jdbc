package nextstep.jdbc.resultset;

import nextstep.exception.resultset.ResultSetExtractFailureException;
import nextstep.jdbc.mapper.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

class ResultSetExtractorTest {

    private final ObjectMapper<User> userMapper = resultSet -> {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        return new User(id, name);
    };

    @DisplayName("ResultSet 에서 리스트를 추출한다.")
    @Test
    void extractList() throws SQLException {
        // given
        ResultSetExtractor<User> resultSetExtractor = new ResultSetExtractor<>(userMapper);

        ResultSet resultSet = mock(ResultSet.class);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getLong("id")).willReturn(1L, 2L);
        given(resultSet.getString("name")).willReturn("ggyool", "oz");

        // when
        List<User> users = resultSetExtractor.extractList(resultSet);

        // then
        then(resultSet).should(times(3)).next();
        then(resultSet).should(times(2)).getLong("id");
        then(resultSet).should(times(2)).getString("name");
        assertThat(users).extracting(User::getId)
                .containsExactly(1L, 2L);
    }

    @DisplayName("ResultSet 에서 단일 객체를 추출한다.")
    @Test
    void extractSingleObject() throws SQLException {
        // given
        ResultSetExtractor<User> resultSetExtractor = spy(new ResultSetExtractor<>(userMapper));
        ResultSet resultSet = mock(ResultSet.class);
        List<User> singleList = Collections.singletonList(new User(1L, "ggyool"));
        willReturn(singleList).given(resultSetExtractor).extractList(resultSet);

        // when
        User user = resultSetExtractor.extractSingleObject(resultSet);

        // then
        then(resultSetExtractor).should(times(1)).extractList(resultSet);
        assertThat(user.getId()).isEqualTo(1L);
    }

    @DisplayName("비어있는 ResultSet 에서 단일 객체를 추출하여 예외가 발생한다. ")
    @Test
    void extractSingleObjectWhenEmptyResultSet() throws SQLException {
        // given
        ResultSetExtractor<User> resultSetExtractor = spy(new ResultSetExtractor<>(userMapper));
        ResultSet resultSet = mock(ResultSet.class);
        List<User> emptyList = Collections.emptyList();
        willReturn(emptyList).given(resultSetExtractor).extractList(resultSet);

        // when, when
        assertThatThrownBy(() -> resultSetExtractor.extractSingleObject(resultSet))
                .isInstanceOf(ResultSetExtractFailureException.class)
                .hasMessageContaining("ResultSet 이 비어있어 객체를 추출하는데 실패했습니다");

        then(resultSetExtractor).should(times(1)).extractList(resultSet);
    }

    @DisplayName("2개 이상의 결과를 가지는 ResultSet 에서 단일 객체를 추출하여 예외가 발생한다. ")
    @Test
    void extractSingleObjectWhenLargeResultSet() throws SQLException {
        // given
        ResultSetExtractor<User> resultSetExtractor = spy(new ResultSetExtractor<>(userMapper));
        ResultSet resultSet = mock(ResultSet.class);
        List<User> list = Arrays.asList(
                new User(1L, "ggyool"),
                new User(2L, "oz")
        );
        willReturn(list).given(resultSetExtractor).extractList(resultSet);

        // when, when
        assertThatThrownBy(() -> resultSetExtractor.extractSingleObject(resultSet))
                .isInstanceOf(ResultSetExtractFailureException.class)
                .hasMessageContaining("ResultSet 의 결과가 하나 이상입니다");

        then(resultSetExtractor).should(times(1)).extractList(resultSet);
    }

    private static class User {
        private final Long id;
        private final String name;

        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }
    }
}
