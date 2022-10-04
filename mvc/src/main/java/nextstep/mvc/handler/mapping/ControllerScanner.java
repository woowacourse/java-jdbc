package nextstep.mvc.handler.mapping;

import java.util.ArrayList;
import java.util.List;

import org.reflections.Reflections;

import nextstep.web.annotation.Controller;

public class ControllerScanner {

	private static final Class<Controller> CONTROLLER_ANNOTATION = Controller.class;

	private ControllerScanner() {
	}

	public static List<Class<?>> getControllers(Object... basePackages) {
		Reflections reflections = new Reflections(basePackages);
		return new ArrayList<>(reflections.getTypesAnnotatedWith(CONTROLLER_ANNOTATION));
	}
}
