package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataEmptyException;
import nextstep.jdbc.exception.DataSizeExcessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.lang.NonNull;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public <T> List<T> query(final String sql, @NonNull final RowMapper<T> rowMapper, final Object... params) {
        return executeQuery(parseRowMapperExecutor(rowMapper), sql, params);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        List<T> result = query(sql, rowMapper, params);
        return singleResult(result);
    }

    public int executeUpdate(final String sql, final Object... params) {
        return executeQuery(PreparedStatement::executeUpdate, sql, params);
    }

    private <T> T executeQuery(QueryExecutor<T> executor, String sql, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            prepareStatementSetParamters(pstmt, params);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement prepareStatementSetParamters(final PreparedStatement pstmt, final Object[] params)
            throws SQLException {
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
        return pstmt;
    }

    private <T> QueryExecutor<List<T>> parseRowMapperExecutor(final RowMapper<T> rowMapper) {
        return pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                return parseResultSet(rowMapper, resultSet, result);
            }
        };
    }

    private <T> List<T> parseResultSet(final RowMapper<T> rowMapper, final ResultSet resultSet, final List<T> result)
            throws SQLException {
        if (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    private <T> T singleResult(final List<T> values) {
        if (values.isEmpty()) {
            throw new DataEmptyException("쿼리 결과가 비어있습니다.");
        }
        if (values.size() > 1) {
            throw new DataSizeExcessException("쿼리 결과가 2개 이상입니다.");
        }
        return values.get(0);
    }
}
