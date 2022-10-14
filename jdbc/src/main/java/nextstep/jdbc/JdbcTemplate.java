package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.Assert;
import nextstep.jdbc.support.DataAccessUtils;
import nextstep.jdbc.support.PreparedStatementCallback;
import nextstep.jdbc.support.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final Object... args) {
        Assert.notNull(sql, "SQL must not be null");
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setArguments(pstmt, args);
            return callback.doInStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public int update(final String sql, @Nullable final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            return DataAccessUtils.objectResult(rowMapper, resultSet);
        }, args);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            final ResultSet resultSet = pstmt.executeQuery();
            return DataAccessUtils.listResult(rowMapper, resultSet);
        }, args);
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        if (args == null) {
            return;
        }

        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
