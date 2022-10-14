package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, pstmt -> updateData(pstmt, args), args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> getData(rowMapper, pstmt, args), args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = execute(sql, pstmt -> getData(rowMapper, pstmt, args), args);
        return getSingleResult(results);
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> executor, final Object... args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setPreparedStatementData(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int index = 1;
        for (Object obj : args) {
            pstmt.setObject(index++, obj);
        }
    }

    private int updateData(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        setPreparedStatementData(pstmt, args);
        return pstmt.executeUpdate();
    }

    private <T> List<T> getData(final RowMapper<T> rowMapper, final PreparedStatement pstmt, final Object[] args)
            throws SQLException {
        setPreparedStatementData(pstmt, args);
        ResultSet resultSet = pstmt.executeQuery();
        return extractData(rowMapper, resultSet);
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final ResultSet resultSet) {
        List<T> result = new ArrayList<>();
        try {
            int rowNum = 0;
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    private <T> T getSingleResult(final List<T> result) {
        if (result.size() == 0) {
            throw new EmptyResultDataAccessException("쿼리 실행 결과가 존재하지 않습니다.");
        }
        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("쿼리 실행 결과의 개수가 초과되었습니다.");
        }
        return result.get(0);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
