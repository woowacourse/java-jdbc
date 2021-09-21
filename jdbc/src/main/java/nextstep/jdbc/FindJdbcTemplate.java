package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class FindJdbcTemplate extends JdbcTemplate {

    public FindJdbcTemplate(DataSource datasource) {
        super(datasource);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new RuntimeException("Not single Result");
        }

        return results.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        Connection conn;
        PreparedStatement pstm;

        try {
            conn = datasource.getConnection();
            pstm = conn.prepareStatement(sql);
        } catch (SQLException e) {
            log.error("SQLException thrown: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        try (
            conn;
            pstm
        ) {
            setSqlArguments(pstm, args);
            return extractResultList(pstm.executeQuery(), rowMapper);
        } catch (SQLException e) {
            log.error("SQLException thrown: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setSqlArguments(PreparedStatement pstm, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstm.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> extractResultList(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        try (rs) {
            while (rs.next()) {
                results.add(rowMapper.apply(rs));
            }
        }

        return results;
    }
}
