package nextstep.jdbc.exception;

public class EmptyResultException extends IncorrectDataSizeException {

    public EmptyResultException(final int expectedSize) {
        super(expectedSize, 0);
    }
}
