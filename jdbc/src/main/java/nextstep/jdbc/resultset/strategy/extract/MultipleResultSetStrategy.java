package nextstep.jdbc.resultset.strategy.extract;

import nextstep.jdbc.mapper.ObjectMapper;
import nextstep.jdbc.resultset.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MultipleResultSetStrategy<T> implements ResultSetExtractStrategy<List<T>> {

    private final ObjectMapper<T> objectMapper;

    public MultipleResultSetStrategy(ObjectMapper<T> objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<T> apply(ResultSet resultSet) throws SQLException {
        ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(objectMapper);
        return resultSetExtractor.extractList(resultSet);
    }
}
