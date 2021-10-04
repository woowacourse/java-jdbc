package nextstep.mvc.exception;

import jakarta.servlet.ServletException;

public class HandlerNotFoundException extends ServletException {

    public HandlerNotFoundException(String message) {
        super(message);
    }
}
