package webmvc.org.springframework.web.servlet.mvc.tobe;

import java.util.ArrayList;
import java.util.List;

public class HandlerAdapters {

    private List<HandlerAdapter> adapters;

    public void init() {
        adapters = new ArrayList<>();
        adapters.add(new RequestMappingHandlerAdapter());
    }

    public HandlerAdapter getHandlerAdapter(final Object handler) {
        return adapters.stream()
                .filter(handlerAdapter -> handlerAdapter.supports(handler))
                .findFirst()
                .orElseThrow(HandlerAdapterNotFoundException::new);
    }
}
