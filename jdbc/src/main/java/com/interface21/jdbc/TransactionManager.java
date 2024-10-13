package com.interface21.jdbc;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import java.sql.Connection;
import java.util.function.Consumer;
import javax.sql.DataSource;

@Component
public class TransactionManager {

    @Inject
    private DataSource dataSource;

    private TransactionManager() {}

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            ConnectionConsumerWrapper.accept(connection, Connection::rollback);
        } finally {
            ConnectionConsumerWrapper.accept(connection, Connection::close);
        }
    }
}
