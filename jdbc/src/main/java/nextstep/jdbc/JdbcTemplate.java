package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.exception.SqlQueryException;
import nextstep.exception.SqlUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        validate(dataSource);
        this.dataSource = dataSource;
    }

    private void validate(DataSource dataSource) {
        if (Objects.isNull(dataSource)) {
            throw new IllegalArgumentException("Empty dataSource!!");
        }
    }

    public void update(String sql, Object... objects) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            PreparedStatementSetter.setValues(pstmt, objects);
            int i = pstmt.executeUpdate();
            LOG.info("update Size : {}", i);
            if (i == 0) {
                throw new SQLException();
            }
        } catch (SQLException e) {
            throw new SqlUpdateException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        List<T> results = query(sql, rowMapper, objects);
        if (results.size() > 1) {
            throw new SqlQueryException();
        }
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... objects) {
        ResultSet resultSet = null;
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter.setValues(pstmt, objects);

            LOG.debug("query : {}", sql);
            resultSet = pstmt.executeQuery();
            RowMapperResultSetExtractor<T> rowMapperResultSetExtractor
                = new RowMapperResultSetExtractor<>(rowMapper);
            return rowMapperResultSetExtractor.extract(resultSet);
        } catch (SQLException e) {
            throw new SqlQueryException(e);
        } finally {
            tryCloseResultSet(resultSet);
        }
    }

    private void tryCloseResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
