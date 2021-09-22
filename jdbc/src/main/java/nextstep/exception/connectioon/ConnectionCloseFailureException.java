package nextstep.exception.connectioon;

import nextstep.exception.DataAccessException;

public class ConnectionCloseFailureException extends DataAccessException {

    public ConnectionCloseFailureException(String message) {
        super(message);
    }
}
