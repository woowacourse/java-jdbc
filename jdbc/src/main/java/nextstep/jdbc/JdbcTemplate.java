package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> mapRow(pstmt, rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return getSingleResult(sql, pstmt -> mapRow(pstmt, rowMapper), args);
    }

    private <T> T getSingleResult(final String sql, final StatementCallback<List<T>> statement, final Object[] args) {
        return execute(sql, statement, args).get(0);
    }

    private <T> List<T> mapRow(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        final List<T> results = new ArrayList<>();
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    public <T> T execute(final String sql, final StatementCallback<T> callback, final Object... args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setObjects(pstmt, args);
            return callback.doInCallableStatement(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void setObjects(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }
}
