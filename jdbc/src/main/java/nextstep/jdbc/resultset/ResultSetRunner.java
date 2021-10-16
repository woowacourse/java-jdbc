package nextstep.jdbc.resultset;

import nextstep.exception.resultset.ResultSetProcessFailureException;
import nextstep.jdbc.resultset.strategy.creation.ResultSetCreationStrategy;
import nextstep.jdbc.resultset.strategy.extract.ResultSetExtractStrategy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetRunner<T> {

    private final ResultSetCreationStrategy resultSetCreationStrategy;
    private final ResultSetExtractStrategy<T> resultSetExtractStrategy;

    public ResultSetRunner(ResultSetCreationStrategy resultSetCreationStrategy, ResultSetExtractStrategy<T> resultSetExtractStrategy) {
        this.resultSetCreationStrategy = resultSetCreationStrategy;
        this.resultSetExtractStrategy = resultSetExtractStrategy;
    }

    public T runWithClose(PreparedStatement pstmt) {
        try (final ResultSet resultSet = resultSetCreationStrategy.create(pstmt)) {
            return resultSetExtractStrategy.apply(resultSet);
        } catch (SQLException sqlException) {
            throw new ResultSetProcessFailureException("ResultSet 생성 또는 추출 중에 문제가 발생했습니다.");
        }
    }
}
