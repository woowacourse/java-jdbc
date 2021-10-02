package nextstep.jdbc.resultset.strategy.extract;

import nextstep.jdbc.mapper.ObjectMapper;
import nextstep.jdbc.resultset.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleResultSetStrategy<T> implements ResultSetExtractStrategy<T> {

    private final ObjectMapper<T> objectMapper;

    public SingleResultSetStrategy(ObjectMapper<T> objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public T apply(ResultSet resultSet) throws SQLException {
        ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(objectMapper);
        return resultSetExtractor.extractSingleObject(resultSet);
    }
}
