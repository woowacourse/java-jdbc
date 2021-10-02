package nextstep.jdbc;

import com.google.common.base.Strings;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.InvalidQueryParameterException;
import nextstep.jdbc.exception.JdbcTemplateSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        List<T> results = queryForList(sql, rowMapper, objects);
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }

        return results.stream()
                .findFirst();
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... objects) {
        validateQuery(sql);
        ResultSetExtractor<List<T>> resultSetExtractor = rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        };

        return queryForList(sql, resultSetExtractor, objects);
    }

    private <T> List<T> queryForList(String sql, ResultSetExtractor<List<T>> resultSetExtractor, Object... objects) {
        return execute(sql, (PreparedStatement pstmt) -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return resultSetExtractor.extractData(rs);
            }
        }, objects);
    }

    public void update(String sql, Object... objects) {
        validateQuery(sql);
        log.info("update query: {}", sql);
        execute(sql, PreparedStatement::executeUpdate, objects);
    }

    private void validateQuery(String sql) {
        if (Strings.isNullOrEmpty(sql)) {
            throw new IllegalArgumentException("Query is Null And Empty!");
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... objects) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setValues(pstmt, objects);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new JdbcTemplateSqlException(e.getMessage());
        }
    }

    private void setValues(PreparedStatement pstmt, Object... objects) {
        IntStream.range(0, objects.length)
                .forEach(index -> setValue(index, pstmt, objects[index]));
    }

    private void setValue(int sequence, PreparedStatement pstmt, Object object) {
        try {
            pstmt.setObject(sequence + 1, object);
        } catch (SQLException e) {
            throw new InvalidQueryParameterException();
        }
    }
}
