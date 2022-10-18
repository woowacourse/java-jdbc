package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionSynchronizationManager {

    private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();
    private final DataSource dataSource;

    public TransactionSynchronizationManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void set(final Connection connection) {
        connectionThreadLocal.set(connection);
    }

    public Connection get() {
        final Connection connection = connectionThreadLocal.get();
        if (connection != null) {
            System.out.println("# (cache) conn = " + connection);
            return connection;
        }
        final Connection newConnection = createConnection();
        set(newConnection);
        System.out.println("# (new) conn = " + newConnection.hashCode());
        return newConnection;
    }

    private Connection createConnection() {
        final Connection newConnection;
        try {
            newConnection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newConnection;
    }

    public void clear() {
        connectionThreadLocal.remove();
    }
}
