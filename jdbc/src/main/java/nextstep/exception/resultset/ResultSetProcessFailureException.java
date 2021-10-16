package nextstep.exception.resultset;

import nextstep.exception.DataAccessException;

public class ResultSetProcessFailureException extends DataAccessException {

    public ResultSetProcessFailureException(String message) {
        super(message);
    }
}
