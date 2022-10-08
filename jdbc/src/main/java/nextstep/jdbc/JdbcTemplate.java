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

    public int update(final String sql, final Object... args) {
        return execute(sql, pstmt -> {
            setArguments(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            setArguments(pstmt, args);
            return createObjects(pstmt, rowMapper);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> resultObjects = query(sql, rowMapper, args);

        if (resultObjects.size() == 0) {
            throw new EmptyResultDataAccessException();
        }

        if (resultObjects.size() != 1) {
            throw new DataAccessException();
        }

        return resultObjects.get(0);
    }

    private <T> T execute(final String sql, final QueryExecutor<T> queryExecutor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> createObjects(final PreparedStatement pstmt, final RowMapper<T> rowMapper)
            throws SQLException {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            return mapObjects(rowMapper, resultSet);
        }
    }

    private <T> List<T> mapObjects(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();

        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }

        return result;
    }
}
