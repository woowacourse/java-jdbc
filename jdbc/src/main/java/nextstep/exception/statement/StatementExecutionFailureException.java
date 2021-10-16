package nextstep.exception.statement;

import nextstep.exception.DataAccessException;

public class StatementExecutionFailureException extends DataAccessException {

    public StatementExecutionFailureException(String message) {
        super(message);
    }
}
