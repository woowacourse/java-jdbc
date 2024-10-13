package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;

public class ConnectionConsumerWrapper {

    public static void accept(Connection connection, ThrowingConnectionConsumer consumer) {
            try {
                if (connection != null) {
                    consumer.accept(connection);
                }
            } catch (Exception e) {
                throw new DataAccessException(e.getMessage(), e);
            }
    }

    private ConnectionConsumerWrapper() {}
}
