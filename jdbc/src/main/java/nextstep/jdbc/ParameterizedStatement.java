package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterizedStatement implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ParameterizedStatement.class);

    private final PreparedStatement preparedStatement;

    private ParameterizedStatement(final PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public static ParameterizedStatement from(final Connection connection, final String sql, final Object... arguments) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int argumentIndex = 1; argumentIndex <= arguments.length; argumentIndex++) {
            preparedStatement.setObject(argumentIndex, arguments[argumentIndex - 1]);
            log.debug("binding parameter [{}] as [{}] - [{}]",
                argumentIndex,
                preparedStatement.getParameterMetaData().getParameterTypeName(argumentIndex),
                arguments[argumentIndex - 1]);
        }

        return new ParameterizedStatement(preparedStatement);
    }

    public PreparedStatement preparedStatementValue() {
        return preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        preparedStatement.close();
    }
}
