package nextstep.mvc;

import java.util.ArrayList;
import java.util.List;
import nextstep.mvc.exception.HandlerAdapterNotFoundException;

public class HandlerAdapters {

    private final List<HandlerAdapter> handlerAdapters;

    public HandlerAdapters() {
        this.handlerAdapters = new ArrayList<>();
    }

    public void add(HandlerAdapter handlerAdapter) {
        handlerAdapters.add(handlerAdapter);
    }

    public HandlerAdapter getAdapter(Object handler) throws HandlerAdapterNotFoundException {
        return handlerAdapters.stream()
            .filter(handlerAdapter -> handlerAdapter.isCompatible(handler))
            .findAny()
            .orElseThrow(HandlerAdapterNotFoundException::new);
    }
}
