package com.techcourse.dao.Strategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FindByIdStrategy implements PreparedStrategy {

    @Override
    public PreparedStatement createStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("select id, account, password, email from users where id = ?");
    }
}
