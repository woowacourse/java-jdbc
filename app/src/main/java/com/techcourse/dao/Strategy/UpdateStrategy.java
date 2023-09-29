package com.techcourse.dao.Strategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateStrategy implements PreparedStrategy {

    @Override
    public PreparedStatement createStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("update users set account = ?, password = ?, email = ? where id = ?");
    }
}
