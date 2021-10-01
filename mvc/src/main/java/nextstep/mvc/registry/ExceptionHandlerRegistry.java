package nextstep.mvc.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import nextstep.mvc.handler.ExceptionMapping;

public class ExceptionHandlerRegistry {

    private final List<ExceptionMapping> exceptionMappings = new ArrayList<>();

    public void addExceptionMapping(ExceptionMapping exceptionMapping) {
        exceptionMappings.add(exceptionMapping);
    }

    public void init() {
        exceptionMappings.forEach(ExceptionMapping::initialize);
    }

    public Optional<Object> getHandle(RuntimeException exception) {
        return exceptionMappings.stream()
            .map(exceptionMapping -> exceptionMapping.getHandler(exception))
            .filter(Objects::nonNull)
            .findFirst();
    }
}
