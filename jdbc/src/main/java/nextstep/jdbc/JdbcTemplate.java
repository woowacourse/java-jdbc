package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final String sql, final JdbcCallback<T> jdbcCallback) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            return jdbcCallback.call(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("커넥션을 가져올 수 없습니다.");
        }
    }

    public <T> List<T> query(final String sql,
                             final RowMapper<T> rowMapper,
                             final Object... args) {
        return execute(sql, preparedStatement -> {
            setParams(preparedStatement, args);
            return executeQuery(preparedStatement, rowMapper);
        });
    }

    public <T> T queryForObject(final String sql,
                                final RowMapper<T> rowMapper,
                                final Object... args) {
        return execute(sql, preparedStatement -> {
            setParams(preparedStatement, args);
            final List<T> result = executeQuery(preparedStatement, rowMapper);
            validateSingleResultSize(result);
            return result.get(0);
        });
    }

    private <T> void validateSingleResultSize(List<T> result) {
        if (result.size() != 1) {
            throw new DataAccessException("조회 결과가 1개가 아닙니다.");
        }
    }

    public void executeUpdate(final String sql,
                              final Object... args) {
        execute(sql, preparedStatement -> {
            setParams(preparedStatement, args);
            return update(preparedStatement);
        });
    }

    private <T> List<T> executeQuery(final PreparedStatement preparedStatement,
                                     final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final List<T> mappedRowResult = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                mappedRowResult.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return mappedRowResult;
        }
    }

    private int update(PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("쿼리를 실행하지 못했습니다.");
        }
    }

    private void setParams(PreparedStatement preparedStatement, Object[] args) {
        int parameterIndex = 1;
        for (Object arg : args) {
            try {
                preparedStatement.setObject(parameterIndex++, arg);
            } catch (SQLException e) {
                throw new DataAccessException("잘못된 파라미터입니다.");
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
