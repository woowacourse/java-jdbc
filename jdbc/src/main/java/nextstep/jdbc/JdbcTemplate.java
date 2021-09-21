package nextstep.jdbc;

import nextstep.jdbc.core.*;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.util.DataAccessUtils;
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

    public int update(String sql, Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    private int update(String sql, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            int count = pstmt.executeUpdate();
            log.debug("update affected rows count : {}", count);
            return count;
        });
    }

    public T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.notNullSingleResult(results);
    }

    public List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), new ArgumentPreparedStatementSetter(args));
    }

    private List<T> query(String sql, RowMapperResultSetExtractor<T> extractor, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractor.extractData(rs);
            }
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }
}
