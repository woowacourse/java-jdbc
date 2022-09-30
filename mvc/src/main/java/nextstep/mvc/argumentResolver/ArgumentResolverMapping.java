package nextstep.mvc.argumentResolver;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public class ArgumentResolverMapping {

	private static final List<ArgumentResolver> argumentResolvers;

	private ArgumentResolverMapping() {
	}

	static {
		Reflections reflections = new Reflections(ArgumentResolverMapping.class.getPackageName());
		argumentResolvers = reflections.getSubTypesOf(ArgumentResolver.class).stream()
			.map(ArgumentResolverMapping::newInstance)
			.collect(Collectors.toList());

	}

	private static ArgumentResolver newInstance(Class<? extends ArgumentResolver> type) {
		try {
			return type.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public static ArgumentResolver getArgumentResolver(Parameter parameter) {
		return argumentResolvers.stream()
			.filter(argumentResolver -> argumentResolver.support(parameter))
			.findFirst()
			.orElseThrow();
	}
}
