package webmvc.org.springframework.web.servlet.mvc.tobe;

import java.util.List;

public class HandlerAdapters {

    private List<HandlerAdapter> handlerAdapters;

    public HandlerAdapters(HandlerAdapter... handlerAdapters) {
        this.handlerAdapters = List.of(handlerAdapters);
    }

    public HandlerAdapter getHandlerAdapter(final Handler handler) {
        return handlerAdapters.stream()
                .filter(adapter -> adapter.isSupport(handler))
                .findFirst()
                .orElse(new DefaultHandlerAdapter());
    }
}
