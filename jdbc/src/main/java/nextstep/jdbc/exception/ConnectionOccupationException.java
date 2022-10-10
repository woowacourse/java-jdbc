package nextstep.jdbc.exception;

public class ConnectionOccupationException extends DataAccessException {

    public ConnectionOccupationException() {
        super("커넥션 점유에 실패하였습니다.");
    }
}
