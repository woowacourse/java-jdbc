package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementCreatorUtils {

    private static final Logger log = LoggerFactory.getLogger(StatementCreatorUtils.class);

    private StatementCreatorUtils() {
    }

    public static void setParameterValue(final PreparedStatement ps, final int parameterPosition, final Object argValue) throws SQLException {
        if (argValue instanceof String) {
            ps.setString(parameterPosition, argValue.toString());
            log.debug("String parameter position : {}, value : {}", parameterPosition, argValue);
            return;
        }

        if (argValue instanceof Long) {
            ps.setLong(parameterPosition, (Long) argValue);
            log.debug("Long parameter position : {}, value : {}", parameterPosition, argValue);
            return;
        }

        if (argValue instanceof Integer) {
            ps.setInt(parameterPosition, (Integer) argValue);
            log.debug("Integer parameter position : {}, value : {}", parameterPosition, argValue);
            return;
        }

        ps.setObject(parameterPosition, argValue);
        log.debug("Object parameter position : {}, value : {}", parameterPosition, argValue);
    }
}
