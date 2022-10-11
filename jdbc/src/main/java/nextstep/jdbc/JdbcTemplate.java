package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_ELEMENT = 0;
    private final TransactionSynchronizationManager synchronizationManager;

    public JdbcTemplate(final TransactionSynchronizationManager synchronizationManager) {
        this.synchronizationManager = synchronizationManager;
    }

    public void execute(final String sql, final Object... params) {
        log.debug("query : {}", sql);
        log.debug("params : {}", params);
        final Connection conn = synchronizationManager.get();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParams(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryOne(final String sql,
                          final RowMapper<T> rowMapper,
                          final Object... conditionParams) {
        final List<T> result = innerQueryAll(sql, rowMapper, conditionParams);
        final int resultSize = result.size();

        if (resultSize == 0) {
            return null;
        } else if (resultSize >= 2) {
            throw new DataAccessException("Expected Result Size One, But Size " + resultSize);
        }

        return result.get(FIRST_ELEMENT);
    }

    public <T> List<T> queryAll(final String sql, final RowMapper<T> userRowMapper) {
        return innerQueryAll(sql, userRowMapper);
    }

    private <T> List<T> innerQueryAll(final String sql, final RowMapper<T> rowMapper, final Object... conditionParams) {
        log.debug("query : {}", sql);
        ResultSet rs = null;
        final Connection conn = synchronizationManager.get();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParams(pstmt, conditionParams);
            rs = pstmt.executeQuery();
            return loadRows(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    private <T> List<T> loadRows(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.map(rs));
        }
        return result;
    }

    private void setStatementParams(final PreparedStatement pstmt, final Object... params) throws SQLException {
        if (params.length == 0) {
            return;
        }

        final int paramSize = List.of(params).size();
        for (int i = 0; i < paramSize; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    private void closeResultSet(final ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
