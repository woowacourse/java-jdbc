package nextstep.mvc.handler.mapping;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

import org.reflections.ReflectionUtils;

import nextstep.web.annotation.RequestMapping;

public class RequestMappingScanner {

	private static final Predicate<Method> HANDLER_PREDICATE = ReflectionUtils.withAnnotation(RequestMapping.class);

	private RequestMappingScanner() {
	}

	public static Set<Method> getHandler(Class<?> controller) {
		return ReflectionUtils.getAllMethods(controller, HANDLER_PREDICATE);
	}
}
