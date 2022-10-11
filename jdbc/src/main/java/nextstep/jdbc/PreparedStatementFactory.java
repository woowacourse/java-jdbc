package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class PreparedStatementFactory {

    private final Connection connection;
    private final PreparedStatement preparedStatement;

    public PreparedStatementFactory(final DataSource dataSource, final String sql) {
        try {
            this.connection = DataSourceUtils.getConnection(dataSource);
            this.preparedStatement = this.connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DataAccessException("[ERROR] PreparedStatementFactory", e);
        }
    }

    public PreparedStatement generatePreparedStatement(final Object[] args) {
        IntStream.range(0, args.length)
                .forEach(index -> setObjectToPreparedStatement(args, preparedStatement, index));
        return preparedStatement;
    }

    private void setObjectToPreparedStatement(final Object[] args, final PreparedStatement preparedStatement, final int index) {
        try {
            preparedStatement.setObject(index + 1, args[index]);
        } catch (SQLException e) {
            throw new DataAccessException("[ERROR] setObjectToPreparedStatement", e);
        }
    }

    public void closeResources() {
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            throw new DataAccessException("[ERROR] closeResources", e);
        }
    }
}
