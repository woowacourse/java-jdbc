package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final PreparedStatementSetter prepareStatementSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = prepareStatementSetter.setPreparedStatement(conn)) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            if (rs.next()) {
                T t = rowMapper.mapRow(rs);
                result.add(t);
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> queryForObject(final PreparedStatementSetter prepareStatementSetter, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = prepareStatementSetter.setPreparedStatement(conn);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return Optional.ofNullable(rowMapper.mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
