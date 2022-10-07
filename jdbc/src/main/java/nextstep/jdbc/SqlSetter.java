package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlSetter<T> {

    private static final Logger log = LoggerFactory.getLogger(SqlSetter.class);

    private final Object[] params;

    public SqlSetter(Object[] params) {
        this.params = params;
    }

    public void injectParams(PreparedStatement preparedStatement) {
        try {
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
