package nextstep.mvc.controller;

import java.util.ArrayList;
import java.util.List;
import nextstep.mvc.HandlerAdapter;
import nextstep.mvc.exception.HandlerAdapterException;

public class HandlerAdapterRegistry {

    private final List<HandlerAdapter> handlerAdapters;

    public HandlerAdapterRegistry() {
        this(new ArrayList<>());
    }

    private HandlerAdapterRegistry(final List<HandlerAdapter> handlerAdapters) {
        this.handlerAdapters = handlerAdapters;
    }

    public void addHandlerAdapter(final HandlerAdapter handlerAdapter) {
        handlerAdapters.add(handlerAdapter);
    }

    public HandlerAdapter getHandlerAdapter(final Object handler) {
        return handlerAdapters.stream()
                .filter(handlerAdapter -> handlerAdapter.supports(handler))
                .findFirst()
                .orElseThrow(() -> new HandlerAdapterException("A matching handler adapter is not found."));
    }
}
