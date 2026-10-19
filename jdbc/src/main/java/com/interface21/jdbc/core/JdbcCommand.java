package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcCommand extends JdbcOperation<Integer> {

    public JdbcCommand(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Integer executeStatement(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeUpdate();
    }
}
