package com.techcourse.dao.Strategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FindStrategy implements PreparedStrategy{

    @Override
    public PreparedStatement createStatement(Connection connection) throws SQLException {
        return null;
    }
}
