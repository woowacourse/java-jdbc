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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql,
                             final RowMapper<T> rowMapper,
                             final Object... args) {
        try (final Connection conn = getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)
        ) {
            setParams(preparedStatement, args);
            return executeQuery(preparedStatement, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("DB 작업 처리 도중 에러가 발생했습니다.");
        }
    }

    public <T> T queryForObject(final String sql,
                                final RowMapper<T> rowMapper,
                                final Object... args) {
        try (final Connection conn = getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)
        ) {
            setParams(preparedStatement, args);
            final List<T> result = executeQuery(preparedStatement, rowMapper);
            validateResultSize(result);
            return result.get(0);
        } catch (SQLException e) {
            throw new DataAccessException("DB 작업 처리 도중 에러가 발생했습니다.");
        }
    }

    private <T> void validateResultSize(List<T> result) {
        if(result.size() != 1){
            throw new DataAccessException("조회 결과가 1개가 아닙니다.");
        }
    }

    public int executeUpdate(final String sql,
                             final Object... args) {
        try(final Connection conn = getConnection();
            final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            setParams(preparedStatement, args);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }

    private void setParams(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        int parameterIndex = 1;
        for (Object arg : args) {
            preparedStatement.setObject(parameterIndex++, arg);
        }
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

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("DB 커넥트 도중 에러가 발생했습니다.");
        }
    }
}
