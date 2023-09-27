package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HandlerMappings {

    private List<HandlerMapping> mappings;

    public void init() {
        mappings = new ArrayList<>();
        mappings.add(new RequestMappingHandlerMapping("com"));
    }

    public HandlerExecution getHandler(final HttpServletRequest request) {
        return mappings.stream()
                .map(handlerMapping -> handlerMapping.getHandler(request))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(HandlerMappingNotFoundException::new);
    }
}
