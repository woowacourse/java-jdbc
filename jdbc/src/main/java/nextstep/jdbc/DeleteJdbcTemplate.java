package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DeleteJdbcTemplate extends JdbcTemplate {

    public DeleteJdbcTemplate(DataSource datasource) {
        super(datasource);
    }

    public void update(String sql, Object... args) {
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
            pstm.executeUpdate();
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
}
