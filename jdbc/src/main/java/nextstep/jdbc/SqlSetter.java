package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class SqlSetter {

    private static final Logger log = LoggerFactory.getLogger(SqlSetter.class);

    public static void injectParams(PreparedStatement preparedStatement, Object... params) {
        try {
            Objects.requireNonNull(params);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }
}
