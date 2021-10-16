package nextstep.exception.connectioon;

import nextstep.exception.DataAccessException;

public class ConnectionAcquisitionFailureException extends DataAccessException {

    public ConnectionAcquisitionFailureException(String message) {
        super(message);
    }
}
