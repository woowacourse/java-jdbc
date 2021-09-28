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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            List<T> targets = new ArrayList<>();

            while (rs.next()) {
                targets.add(rowMapper.mapRow(rs));
            }
            rs.close();
            return targets;
        }, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        final List<T> result = queryForList(sql, rowMapper, args);
        if (result.isEmpty()) {
            throw new EmptyResultDataAccessException();
        }
        if (result.size() > 1) {
            log.info("Size is {}", result.size());
            throw new IncorrectResultSizeDataAccessException();
        }
        return result.get(0);
    }

    private <T> T execute(String sql, Execute<T> logic, Object... args) {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setArguments(pstmt, args);
            return logic.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }

    private void setArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface Execute<T> {

        T execute(PreparedStatement pstmt) throws SQLException;
    }
}
