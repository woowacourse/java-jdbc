package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimplePreparedStatementCreator implements PreparedStatementCreator {

    private final String sql;

    public SimplePreparedStatementCreator(final String sql) {
        this.sql = sql;
    }

    @Override
    public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
        return conn.prepareStatement(sql);
    }
}
