package nextstep.jdbc.exception;

public class InvalidDataSizeException extends DataAccessException {

    public InvalidDataSizeException(final int expected, final int actual) {
        super(String.format("예상하지 못한 데이터 개수입니다 : 예상 %d개, 실제 %d개", expected, actual));
    }
}
