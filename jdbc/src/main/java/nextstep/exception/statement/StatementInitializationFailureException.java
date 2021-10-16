package nextstep.exception.statement;

import nextstep.exception.DataAccessException;

public class StatementInitializationFailureException extends DataAccessException {

    public StatementInitializationFailureException(String message) {
        super(message);
    }
}
