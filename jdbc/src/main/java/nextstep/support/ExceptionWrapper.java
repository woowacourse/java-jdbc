package nextstep.support;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionWrapper {

    public static <T> T get(final Callable<T> throwableCall) {
        try {
            return throwableCall.call();
        } catch (Exception e) {
            final Logger log = LoggerFactory.getLogger(throwableCall.getClass());
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}
