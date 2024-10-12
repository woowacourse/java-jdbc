package com.techcourse.service;


import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFunctionWrapper {

    private ConnectionFunctionWrapper() {
    }

    public static void accept(Connection connection, ConnectionConsumer function) {
        try {
            if(connection != null) {
                function.accept(connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
