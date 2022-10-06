package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final StatementCallback<T> statementCallback, final PreparedStatement pstmt) {
        return statementCallback.doInStatement(pstmt);
    }

    private <T> T query(final String sql, final ResultSetExtractor<T> resultSetExtractor, final Object... objects)
            throws DataAccessException {
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(sql, objects);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementSetter.createPreparedStatement(connection)) {

            class QueryStatementCallback implements StatementCallback<T> {

                @Override
                public T doInStatement(final PreparedStatement pstmt) {
                    try (ResultSet resultSet = pstmt.executeQuery()) {
                        return resultSetExtractor.extractData(resultSet);
                    } catch (SQLException e) {
                        throw new DataAccessException("query exception", e);
                    }
                }
            }
            return execute(new QueryStatementCallback(), pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("query exception", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... objects)
            throws DataAccessException {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), objects);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... objects)
            throws DataAccessException {
        return DataAccessUtils.nullableSingleResult(query(sql, new RowMapperResultSetExtractor<>(rowMapper), objects));
    }

    public int update(final String sql, final Object... objects) throws DataAccessException {
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(sql, objects);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementSetter.createPreparedStatement(connection)) {

            class UpdateStatementCallback implements StatementCallback<Integer> {

                @Override
                public Integer doInStatement(final PreparedStatement pstmt) {
                    try {
                        return pstmt.executeUpdate();
                    } catch (SQLException e) {
                        throw new DataAccessException("update Error", e);
                    }
                }
            }
            return execute(new UpdateStatementCallback(), pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("update exception", e);
        }
    }
}
