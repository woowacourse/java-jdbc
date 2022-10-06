package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(sql, args, connection)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("[ERROR] update", e);
        }
    }

    private PreparedStatement generatePreparedStatement(
            final String sql, final Object[] args, final Connection connection
    ) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        IntStream.range(0, args.length)
                .forEach(index -> setObjectToPreparedStatement(args, pstmt, index));
        return pstmt;
    }

    private void setObjectToPreparedStatement(final Object[] args, final PreparedStatement pstmt, final int index) {
        try {
            pstmt.setObject(index + 1, args[index]);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("[ERROR] setObjectToPreparedStatement", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(sql, args, connection);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            return rowMapper.mapping(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("[ERROR] queryForObject", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(sql, new Object[0], connection);
             ResultSet rs = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            if (rs.next()) {
                result.add(rowMapper.mapping(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("[ERROR] query", e);
        }
    }
}
