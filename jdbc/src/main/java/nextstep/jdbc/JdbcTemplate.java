package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        execute(sql, pstmt -> {
            setParameters(args, pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> queryForList(final String sql, RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            setParameters(args, pstmt);
            return getResult(pstmt, rowMapper);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            setParameters(args, pstmt);
            List<T> result = getResult(pstmt, rowMapper);
            checkResultSizeIsOne(result);
            return result.iterator().next();
        });
    }

    private <T> T execute(String sql, PreparedStater<T> strategy) {
        try (Connection conn = DataSourceUtils.getConnection(dataSource);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return strategy.doStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Query 에러가 발생했습니다.");
        }
    }

    private <T> List<T> getResult(final PreparedStatement preparedStatement,
                                  final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return ResultSetExtractor.extractData(resultSet, rowMapper);
        }
    }

    private <T> void checkResultSizeIsOne(final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("결과가 없습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("결과가 2개 이상입니다.");
        }
    }

    private void setParameters(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }
}
