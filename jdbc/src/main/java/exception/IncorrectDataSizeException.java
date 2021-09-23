package exception;

public class IncorrectDataSizeException extends RuntimeException{
    public IncorrectDataSizeException(int expected, int actual) {
        super(String.format("expected: %d, actual: %d", expected, actual));
    }
}
