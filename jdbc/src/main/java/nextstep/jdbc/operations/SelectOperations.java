package nextstep.jdbc.operations;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.utils.ResultSetExtractor;
import nextstep.jdbc.utils.RowMapper;

public interface SelectOperations {

    <T> List<T> queryForList(String sql, Class<T> type) throws SQLException;

    <T> List<T> query(String sql, RowMapper<T> rowMapper) throws SQLException;

    <T> T query(String sql, ResultSetExtractor<T> rse) throws SQLException;

    <T> Optional<T> queryForObject(String sql, Class<T> type) throws SQLException;

    <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper) throws SQLException;
}
