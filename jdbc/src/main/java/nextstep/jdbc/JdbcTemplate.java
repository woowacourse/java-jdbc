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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... args) {
        prepareStatement(sql, PreparedStatement::execute, args);
    }

    public Integer executeUpdate(final String sql, final Object... args) {
        return prepareStatement(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> queryAsList(final String sql, final RowMapper<T> rowMapper) {
        final JdbcCallback<List<T>> callback = pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(rowMapper.mapLow(rs, 0));
                }

                return result;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        };

        return prepareStatement(sql, callback);
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final JdbcCallback<T> callback = pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapLow(rs, 0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        };

        return prepareStatement(sql, callback, args);
    }

    private <T> T prepareStatement(final String sql, JdbcCallback<T> callback, final Object... args) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            prepareStatementSetObject(pstmt, args);

            return callback.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void prepareStatementSetObject(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
