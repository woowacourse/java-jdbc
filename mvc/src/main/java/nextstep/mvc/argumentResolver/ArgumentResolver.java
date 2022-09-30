package nextstep.mvc.argumentResolver;

import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ArgumentResolver {

	boolean support(Parameter parameter);

	Object resolve(HttpServletRequest request, HttpServletResponse response, Parameter parameter);
}
