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
        ResultSet resultSet = null;
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            setArguments(pstmt, args);

            resultSet = pstmt.executeQuery();
            return convertObjects(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(resultSet);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, rowMapper, args);
        return extractOne(sql, result);
    }

    public int update(final String sql, final Object... args) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            setArguments(pstmt, args);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object... args) throws SQLException {
        final List<Object> arguments = List.of(args);
        int index = 1;
        for (final Object argument : arguments) {
            pstmt.setObject(index++, argument);
        }
    }

    private <T> List<T> convertObjects(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.convertObject(resultSet));
        }
        return results;
    }

    private <T> T extractOne(final String sql, final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException(String.format("조회 결과가 없습니다. [%s]", sql));
        }
        if (result.size() > 1) {
            throw new DataAccessException(String.format("조회 결과가 1개 이상입니다. [%s]", sql));
        }
        return result.get(0);
    }

    private void closeResultSet(final ResultSet resultSet) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }
}
