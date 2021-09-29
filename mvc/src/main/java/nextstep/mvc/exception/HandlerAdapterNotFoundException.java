package nextstep.mvc.exception;

import jakarta.servlet.ServletException;

public class HandlerAdapterNotFoundException extends ServletException {

    public HandlerAdapterNotFoundException(String message) {
        super(message);
    }
}
