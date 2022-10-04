package nextstep.jdbc;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException(final int expectedSize) {
        super(expectedSize, 0);
    }
}
