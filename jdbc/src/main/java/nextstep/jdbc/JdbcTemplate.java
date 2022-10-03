package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(final String sql, Object... parameters) {
        validateSql(sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            log.debug("query : {}", sql);
            setParameter(pstmt, parameters);
            pstmt.executeUpdate();
            return extractId(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rse, final Object... parameters) {
        validateSql(sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(pstmt, parameters);
            return validateNotNull(extractInstance(rse, pstmt));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rse, final Object... conditions) {
        return DataAccessUtils.nullableSingleResult(query(sql, rse, conditions));
    }

    public void update(final String sql, Object... parameters) {
        validateSql(sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(pstmt, parameters);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void validateSql(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }
    }

    private long extractId(PreparedStatement pstmt) throws SQLException {
        try (final ResultSet rs = pstmt.getGeneratedKeys()) {
            rs.next();
            return rs.getLong("id");
        }
    }

    private <T> List<T> validateNotNull(List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("No Such Result");
        }
        return result;
    }

    private void setParameter(PreparedStatement pstmt, Object... conditions) throws SQLException {
        for (int i = 0; i < conditions.length; i++) {
            pstmt.setObject(i + 1, conditions[i]);
        }
    }

    private <T> List<T> extractInstance(RowMapper<T> rse, PreparedStatement pstmt) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        try (final ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                results.add(rse.mapRow(rs, rowNum++));
            }
            return results;
        }
    }
}
