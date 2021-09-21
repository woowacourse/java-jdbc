package nextstep.jdbc;

import nextstep.jdbc.core.ArgumentPreparedStatementSetter;
import nextstep.jdbc.core.PreparedStatementSetter;
import nextstep.jdbc.core.RowMapper;
import nextstep.jdbc.core.RowMapperResultSetExtractor;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.NotSingleResultDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class JdbcTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource is required");
    }

    public void update(String sql, Object... args) {
        update(sql, new ArgumentPreparedStatementSetter(args));
    }

    private void update(String sql, PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pss.setValues(pstmt);
            pstmt.executeUpdate();
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        validateSingle(results);
        return results.iterator().next();
    }

    public List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), new ArgumentPreparedStatementSetter(args));
    }

    private List<T> query(String sql, RowMapperResultSetExtractor<T> extractor, PreparedStatementSetter pss) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pss.setValues(pstmt);
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return extractor.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        } finally {
            closeResultSet(rs);
        }
    }

    private void validateSingle(List<T> results) {
        if (Objects.isNull(results) || results.isEmpty()) {
            throw new NotSingleResultDataException("Result is Empty");
        }
        if (results.size() > 1) {
            throw new NotSingleResultDataException("Result size is " + results.size());
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
