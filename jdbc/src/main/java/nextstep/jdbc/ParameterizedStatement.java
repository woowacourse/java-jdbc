package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
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
        for (int i = 0; i < arguments.length; i++) {
            preparedStatement.setObject(i + 1, arguments[i]);
            log.debug("binding parameter [{}] as [{}] - [{}]",
                i + 1,
                preparedStatement.getParameterMetaData().getParameterTypeName(i + 1),
                arguments[i]);
        }

        return new ParameterizedStatement(preparedStatement);
    }

    public PreparedStatement preparedStatementValue() {
        return preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        if (Objects.nonNull(preparedStatement)) {
            preparedStatement.close();
            log.debug("ParameterizedStatement is closed.");
        }
    }
}
