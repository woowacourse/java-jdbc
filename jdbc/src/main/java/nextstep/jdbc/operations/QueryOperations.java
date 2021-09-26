package nextstep.jdbc.operations;

import java.util.List;
import java.util.Optional;
import nextstep.jdbc.utils.ResultSetExtractor;
import nextstep.jdbc.utils.RowMapper;

public interface QueryOperations {

    <T> List<T> queryForList(String sql, Class<T> type, Object ... args);

    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ... args);

    <T> T query(String sql, ResultSetExtractor<T> rse, Object ... args);

    <T> Optional<T> queryForObject(String sql, Class<T> type, Object ... args);

    <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object ... args);

    int update(String sql, Object ... args);
}
