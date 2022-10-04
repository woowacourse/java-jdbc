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
    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return getSingleResult(results);
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.size() != SINGLE_RESULT_SIZE) {
            throw new DataAccessException("조회한 결과의 크기가 1이 아닙니다.");
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();

            return getResult(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        rs.close();
        return results;
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int index = SINGLE_RESULT_SIZE;
        for (Object arg : args) {
            pstmt.setObject(index, arg);
            index++;
        }
    }
}
