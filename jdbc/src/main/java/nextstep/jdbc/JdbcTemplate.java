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
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(new SimplePreparedStatementSetter(sql, args));
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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(rowMapper, new SimplePreparedStatementSetter(sql, EMPTY_ARGUMENTS));
    }

    private <T> List<T> query(final RowMapper<T> rowMapper, final SimplePreparedStatementSetter spss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = spss.setPreparedStatement(conn);
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

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = query(rowMapper, new SimplePreparedStatementSetter(sql, args));
        if (result.size() != 1) {
            throw new RuntimeException();
        }
        return Optional.ofNullable(result.get(0));
    }
}
