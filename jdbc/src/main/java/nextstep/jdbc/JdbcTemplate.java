package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.SQLAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return queryList(sql, rowMapper);
    }

    private <T> T execute(String sql, ExecuteStrategy<T> strategy, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            putArguments(pstmt, args);
            return strategy.execute(pstmt);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLAccessException();
        }
    }

    private <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            putArguments(pstmt, args);

            List<T> results = extractResult(rowMapper, pstmt);
            validSingleResult(results);
            return results.get(0);
        } catch (SQLException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new SQLAccessException();
        }
    }

    private <T> void validSingleResult(List<T> results) {
        if (CollectionUtils.isEmpty(results)) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
    }

    private <T> List<T> queryList(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return extractResult(rowMapper, pstmt);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLAccessException();
        }
    }

    private <T> List<T> extractResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet resultSet = pstmt.executeQuery();
        List<T> result = new ArrayList<>();
        for (int i = 0; resultSet.next(); i++) {
            result.add(rowMapper.mapRow(resultSet, i));
        }
        return result;
    }

    private void putArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
