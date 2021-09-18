package nextstep.jdbc.connector;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DbConnectorImpl implements DbConnector {

    private final DataSource dataSource;

    public DbConnectorImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Connection 연결 도중 오류가 발생했습니다.", e);
        }
    }
}
