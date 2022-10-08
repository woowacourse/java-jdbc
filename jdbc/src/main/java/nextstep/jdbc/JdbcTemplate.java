package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQueryTemplate(sql, pstmt -> {
            setArguments(pstmt, args);
            return executeQuery(pstmt, rowMapper);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, rowMapper, args);
        return extractOne(result);
    }

    public int update(final String sql, final Object... args) {
        return executeQueryTemplate(sql, pstmt -> {
            setArguments(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    private <T> T executeQueryTemplate(final String sql, final QueryExecutor<T> queryExecutor) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return queryExecutor.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object... args) throws SQLException {
        int index = 1;
        for (final Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            return convertObjects(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> convertObjects(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> T extractOne(final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("조회 결과가 없습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("조회 결과가 1개 이상입니다.");
        }
        return result.get(0);
    }
}
