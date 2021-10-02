package nextstep.jdbc.resultset;

import nextstep.jdbc.resultset.strategy.creation.ResultSetCreationStrategy;
import nextstep.jdbc.resultset.strategy.extract.ResultSetExtractStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class ResultSetRunnerTest {

    @DisplayName("생성 전략으로 ResultSet 을 생성하고 추출 전략으로 추출한다. 추출 후 ResultSet 을 close 한다.")
    @Test
    void test() throws SQLException {
        // given
        ResultSetCreationStrategy resultSetCreationStrategy = mock(ResultSetCreationStrategy.class);
        ResultSetExtractStrategy<User> resultSetExtractStrategy = mock(ResultSetExtractStrategy.class);
        ResultSet resultSet = mock(ResultSet.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        given(resultSetCreationStrategy.create(preparedStatement)).willReturn(resultSet);
        given(resultSetExtractStrategy.apply(resultSet)).willReturn(new User(1L, "user"));

        // when
        ResultSetRunner<User> resultSetRunner = new ResultSetRunner<>(resultSetCreationStrategy, resultSetExtractStrategy);
        resultSetRunner.runWithClose(preparedStatement);

        // then
        InOrder inOrder = inOrder(resultSetCreationStrategy, resultSetExtractStrategy, resultSet);
        then(resultSetCreationStrategy).should(inOrder, times(1)).create(preparedStatement);
        then(resultSetExtractStrategy).should(inOrder, times(1)).apply(resultSet);
        then(resultSet).should(inOrder, times(1)).close();
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
