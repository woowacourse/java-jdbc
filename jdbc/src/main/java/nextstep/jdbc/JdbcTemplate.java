package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, @Nullable Object... args) {
        PreparedStatementSetter preparedStatementSetter = new ArgumentTypePreparedStatementSetter(args);
        return execute(sql, pstmt -> {
            preparedStatementSetter.setValue(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        List<T> results = query(sql,
                new ArgumentTypePreparedStatementSetter(args),
                new ResultSetExtractor<>(rowMapper));
        if (results.size() >= 2) {
            throw new TooManyResultsException();
        }
        return results.stream()
                .findFirst()
                .orElseThrow(EmptyResultException::new);
    }

    private <T> List<T> query(String sql,
                              PreparedStatementSetter preparedStatementSetter,
                              ResultSetExtractor<T> resultSetExtractor) {
        PreparedStatementCallback<List<T>> callback = pstmt -> {
            try (ResultSet rs = createResultSet(pstmt, preparedStatementSetter)) {
                return resultSetExtractor.extract(rs);
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }};
        return execute(sql, callback);
    }

    private ResultSet createResultSet(PreparedStatement preparedStatement,
                                      PreparedStatementSetter preparedStatementSetter) throws SQLException {
        preparedStatementSetter.setValue(preparedStatement);
        return preparedStatement.executeQuery();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, new ResultSetExtractor<>(rowMapper));
    }

    private <T> List<T> query(String sql,
                              ResultSetExtractor<T> resultSetExtractor) {
        PreparedStatementCallback<List<T>> callback = pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return resultSetExtractor.extract(rs);
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }};
        return execute(sql, callback);
    }

    private <T> T execute(String sql,
                          PreparedStatementCallback<T> preparedStatementCallback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

}
