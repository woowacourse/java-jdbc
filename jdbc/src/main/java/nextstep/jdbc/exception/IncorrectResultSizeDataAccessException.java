package nextstep.jdbc.exception;

import nextstep.jdbc.exception.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException() {
        super("Expected single data, but it was more than one result!");
    }
}
