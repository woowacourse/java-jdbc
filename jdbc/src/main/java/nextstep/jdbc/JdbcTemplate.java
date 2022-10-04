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

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            setPreparedStatement(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatement(pstmt, args);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs, 0);
            }
            return null;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            ResultSet rs = pstmt.executeQuery();

            return assembleResult(rs, rowMapper);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> assembleResult(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        int rowNum = 0;
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rowNum));
            rowNum++;
        }
        return result;
    }

    private void setPreparedStatement(PreparedStatement pstmt, Object... args) throws SQLException {

        int index = 0;
        for (Object arg : args) {
            index++;
            switch (arg.getClass().getName()) {
                case "String":
                    pstmt.setString(index, (String)arg);
                    break;
                case "Long":
                    pstmt.setLong(index, (Long)arg);
                    break;
                default:
                    pstmt.setObject(index, arg);
            }
        }
    }
}
