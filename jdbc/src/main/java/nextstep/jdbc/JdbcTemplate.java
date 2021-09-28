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

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            preparedStatementSetter.setValues(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlUpdateException(e);
        }
    }

    public int update(String sql, Object... objects) {
        return update(sql, createPreparedStatementSetter(objects));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper,
        PreparedStatementSetter preparedStatementSetter) {
        List<T> results = query(sql, rowMapper, preparedStatementSetter);
        if (results.size() > 1) {
            throw new SqlQueryException();
        }
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        return queryForObject(sql, rowMapper, createPreparedStatementSetter(objects));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper,
        PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet resultSet = executeQuery(pstmt, preparedStatementSetter)) {
            LOG.debug("query : {}", sql);
            RowMapperResultSetExtractor<T> rowMapperResultSetExtractor
                = new RowMapperResultSetExtractor<>(rowMapper);
            return rowMapperResultSetExtractor.extract(resultSet);
        } catch (SQLException e) {
            throw new SqlQueryException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... objects) {
        return query(sql, rowMapper, createPreparedStatementSetter(objects));
    }

    private ResultSet executeQuery(PreparedStatement pstmt,
        PreparedStatementSetter preparedStatementSetter) throws SQLException {
        preparedStatementSetter.setValues(pstmt);
        return pstmt.executeQuery();
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object[] objects) {
        return pstmt -> {
            for (int i = 0; i < objects.length; i++) {
                pstmt.setObject(i + 1, objects[i]);
            }
        };
    }
}
