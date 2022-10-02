package nextstep.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerExecutor {

    private static final Logger log = LoggerFactory.getLogger(HandlerExecutor.class);

    private final HandlerAdapterRegistry handlerAdapterRegistry;

    public HandlerExecutor(final HandlerAdapterRegistry handlerAdapterRegistry) {
        this.handlerAdapterRegistry = handlerAdapterRegistry;
    }

    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        final var handlerAdapter = handlerAdapterRegistry.getHandlerAdapter(handler);
        return handlerAdapter.handle(request, response, handler);
    }
}
