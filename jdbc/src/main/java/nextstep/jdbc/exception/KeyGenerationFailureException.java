package nextstep.jdbc.exception;

import java.sql.SQLException;

public class KeyGenerationFailureException extends DataAccessException {

    public KeyGenerationFailureException(SQLException exception) {
        super(exception);
    }
}
