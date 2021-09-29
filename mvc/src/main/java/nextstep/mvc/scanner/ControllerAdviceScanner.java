package nextstep.mvc.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.mvc.exception.InternalServerException;
import nextstep.web.annotation.ControllerAdvice;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerAdviceScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerAdviceScanner.class);

    private final Reflections reflections;
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public ControllerAdviceScanner(Reflections reflections) {
        this.reflections = reflections;
    }

    public void findControllerAdvice() {
        try {
            Set<Class<?>> controllerAdvices = reflections
                .getTypesAnnotatedWith(ControllerAdvice.class);
            for (Class<?> clazz : controllerAdvices) {
                beans.put(clazz, clazz.getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            LOG.error("Instance Create Error!! : {}", e.getMessage());
            throw new InternalServerException();
        }
    }

    public Set<Class<?>> getControllerAdviceClasses() {
        return beans.keySet();
    }

    public Object getInstance(Class<?> key) {
        return beans.get(key);
    }
}
