package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(PreparedStatementCreator psc) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = psc.createPreparedStatement(conn);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            //todo; Custom Exception 하나 만들어서 통일하기!
            throw new IllegalArgumentException("");
        }
    }

//    public <T> T update(ConnectionCallback<T> action) {
//        assert action == null : "Callback object must not be null";
//        String sql = "";
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            action.doInConnection(conn);
//        } catch (SQLException e) {
//
//        }
//    }
}
