package nextstep.mvc.registry;

import java.util.ArrayList;
import java.util.List;
import nextstep.mvc.adapter.HandlerAdapter;
import nextstep.mvc.exception.BadRequestException;

public class HandlerAdapterRegistry {

    private final List<HandlerAdapter> handlerAdapters;

    public HandlerAdapterRegistry() {
        this.handlerAdapters = new ArrayList<>();
    }

    public void addHandlerAdapter(HandlerAdapter handlerAdapter) {
        handlerAdapters.add(handlerAdapter);
    }

    public HandlerAdapter getHandlerAdapter(Object handle) {
        return handlerAdapters.stream()
            .filter(handlerAdapter -> handlerAdapter.supports(handle))
            .findFirst()
            .orElseThrow(BadRequestException::new);
    }
}
