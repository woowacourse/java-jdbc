package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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

    public int update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setValues(preparedStatement, args);

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    private void setValues(final PreparedStatement preparedStatement, final Object[] args) throws SQLException {
        for (int idx = 0; idx < args.length; idx++) {
            preparedStatement.setObject(idx + 1, args[idx]);
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            setValues(preparedStatement, args);
            final RowMapperResultSetExtractor<T> resultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (CollectionUtils.isEmpty(results)) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator()
                .next();
    }
}
