package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.DataEmptyException;
import nextstep.jdbc.exception.DataSizeExcessException;
import nextstep.jdbc.exception.ResultSetCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public <T> List<T> query(final String sql, @NonNull final RowMapper<T> rowMapper, final Object... params) {
        ResultSet resultSet = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            prepareStatementSetParamters(pstmt, params);

            resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            return parseResultSet(rowMapper, resultSet, result);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(resultSet);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        List<T> result = query(sql, rowMapper, params);
        return singleResult(result);
    }

    public int executeUpdate(final String sql, final Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            prepareStatementSetParamters(pstmt, params);
            return pstmt.executeUpdate();
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

    private <T> List<T> parseResultSet(final RowMapper<T> rowMapper, final ResultSet resultSet, final List<T> result)
            throws SQLException {
        if (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    private void closeResultSet(final ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ignored) {
            throw new ResultSetCloseException("result set close 에러");
        }
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
