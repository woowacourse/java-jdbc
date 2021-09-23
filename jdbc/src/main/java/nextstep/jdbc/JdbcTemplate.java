package nextstep.jdbc;

import exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public int update(String sql, Object... args) {
        return execute(
            conn -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
                pss.setValues(preparedStatement);
                return preparedStatement;
            }
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
     return result(query(sql, new RowMapperResultSetExtractor<>(rowMapper)));
    }

    private <T> List<T> result(List<T> query) {
        return query;
    }

    public <T> List<T> query(final String sql, final RowMapperResultSetExtractor<T> rse) {
        return execute(conn -> conn.prepareStatement(sql), rse);
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> result = query(sql, args, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return DataAccessUtils.singleResult(result);
    }

    private <T> List<T> query(String sql, Object[] args, RowMapperResultSetExtractor<T> rse) {
        return query(sql, new ArgumentPreparedStatementSetter(args), rse);
    }

    private <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapperResultSetExtractor<T> rse) {
        return execute(
            conn -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                pss.setValues(preparedStatement);
                return preparedStatement;
            },
            rse
        );
    }

    private int execute(PreparedStatementStrategy strategy) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = strategy.makePreparedStatement(conn))
        {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> execute(PreparedStatementStrategy strategy, RowMapperResultSetExtractor<T> rse) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = strategy.makePreparedStatement(conn);
            ResultSet rs = pstmt.executeQuery())
        {
            return rse.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
