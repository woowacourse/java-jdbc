package com.interface21.jdbc.core;


import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class ConnectionConsumerWrapper {

    private ConnectionConsumerWrapper() {
    }

    public static void accept(Connection connection, ConnectionConsumer consumer) {
        try {
            if (!Objects.isNull(consumer)) {
                consumer.accept(connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
