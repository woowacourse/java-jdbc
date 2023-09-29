package com.techcourse.dao.Strategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertStrategy implements PreparedStrategy {

    @Override
    public PreparedStatement createStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("insert into users (account, password, email) values (?, ?, ?)");
    }
}
