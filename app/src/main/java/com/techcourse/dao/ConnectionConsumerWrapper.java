package com.techcourse.dao;


import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionConsumerWrapper {

    private ConnectionConsumerWrapper() {
    }

    public static void accept(Connection connection, ConnectionConsumer consumer) {
        try {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
