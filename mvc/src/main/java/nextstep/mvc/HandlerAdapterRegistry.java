package nextstep.mvc;

import java.util.ArrayList;
import java.util.List;
import nextstep.mvc.controller.exception.NotSupportHandler;

public class HandlerAdapterRegistry {

    private final List<HandlerAdapter> handlerAdapters;

    public HandlerAdapterRegistry() {
        this.handlerAdapters = new ArrayList<>();
    }

    public void addHandlerAdapter(final HandlerAdapter adapter) {
        handlerAdapters.add(adapter);
    }

    public HandlerAdapter getHandlerAdapter(final Object handler) {
        return handlerAdapters.stream()
                .filter(handlerAdapter -> handlerAdapter.supports(handler))
                .findAny()
                .orElseThrow(NotSupportHandler::new);
    }
}
