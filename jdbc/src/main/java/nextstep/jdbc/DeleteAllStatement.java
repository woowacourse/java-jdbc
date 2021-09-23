package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAllStatement implements StatementStrategy{
    private static final Logger log = LoggerFactory.getLogger(DeleteAllStatement.class);
    @Override
    public PreparedStatement makePreparedStatement(Connection conn) throws SQLException {
        String sql = "delete from users";
        log.debug("query : {}", sql);
        return conn.prepareStatement(sql);
    }
}
