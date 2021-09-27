package nextstep.jdbc.exception;

import java.sql.SQLException;

public class JdbcTemplateException extends RuntimeException{

    public JdbcTemplateException(final SQLException e) {
        super(e.getMessage());
    }
}
