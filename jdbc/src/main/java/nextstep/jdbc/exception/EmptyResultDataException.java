package nextstep.jdbc.exception;

import java.sql.SQLException;
import nextstep.jdbc.exception.DataAccessException;

public class EmptyResultDataException extends DataAccessException {

    public EmptyResultDataException() {
        super("Expected single data, but it was empty result!");
    }

    public EmptyResultDataException(SQLException sqlException) {
        super(sqlException);
    }
}
