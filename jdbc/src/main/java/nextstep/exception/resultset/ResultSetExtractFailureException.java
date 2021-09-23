package nextstep.exception.resultset;

import nextstep.exception.DataAccessException;

public class ResultSetExtractFailureException extends DataAccessException {

    public ResultSetExtractFailureException(String message) {
        super(message);
    }
}
