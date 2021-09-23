package nextstep.jdbc;

import com.techcourse.support.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParams(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParams(pstmt, params);
            rs = pstmt.executeQuery();

            return mapToList(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) throws DataAccessException {
        List<T> result = query(sql, rowMapper, params);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    private void setParams(PreparedStatement pstmt, Object[] params) throws SQLException {
        int firstIdx = 1;
        for (Object param : params) {
            pstmt.setObject(firstIdx++, param);
        }
    }

    private <T> ArrayList<T> mapToList(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        ArrayList<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rowMapper.map(rs));
        }
        return list;
    }
}
