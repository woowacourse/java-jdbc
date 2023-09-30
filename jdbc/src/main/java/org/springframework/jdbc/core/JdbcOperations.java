package org.springframework.jdbc.core;


import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JdbcOperations {

    void execute(final String sql) throws DataAccessException;

    <T> T execute(final PreparedStatementCallback<T> preparedStatementCallback)
        throws DataAccessException, SQLException;

    <T> T execute(final StatementCallback<T> callback) throws DataAccessException;

    <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException;

    <T> T query(final String sql, final ResultSetExtractor<T> extractor) throws DataAccessException;

    <T> T query(final String sql, final RowMapper<T> rowMapper) throws DataAccessException;

    int update(final String sql, final Object... args) throws DataAccessException;

    <T> List<T> queryForList(final String sql, final Class<T> elementType) throws DataAccessException;
}
