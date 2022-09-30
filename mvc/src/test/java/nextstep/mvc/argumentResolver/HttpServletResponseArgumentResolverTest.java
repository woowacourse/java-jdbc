package nextstep.mvc.argumentResolver;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class HttpServletResponseArgumentResolverTest {

	@DisplayName("HttpServletResponse를 지원하는 resolver인지 확인한다.")
	@Test
	void support_true() throws NoSuchMethodException {
		// given
		Method thisMethod = getClass().getDeclaredMethod("response", HttpServletResponse.class);

		// when
		ArgumentResolver resolver = new HttpServletResponseArgumentResolver();
		boolean actual = resolver.support(thisMethod.getParameters()[0]);

		// then
		assertThat(actual).isTrue();
	}

	@DisplayName("HttpServletResponse를 지원하지 않는 resolver인지 확인한다.")
	@Test
	void support_false() throws NoSuchMethodException {
		// given
		Method method = getClass().getDeclaredMethod("request", HttpServletRequest.class);

		// when
		ArgumentResolver resolver = new HttpServletResponseArgumentResolver();
		boolean actual = resolver.support(method.getParameters()[0]);

		// then
		assertThat(actual).isFalse();
	}

	@DisplayName("HttpServletRequest를 resolve한다.")
	@Test
	void resolve() throws NoSuchMethodException {
		// given
		Method method = getClass().getDeclaredMethod("response", HttpServletResponse.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// when
		ArgumentResolver resolver = new HttpServletResponseArgumentResolver();
		Object actual = resolver.resolve(request, response, method.getParameters()[0]);

		// then
		assertThat(actual).isEqualTo(response);
	}

	void request(HttpServletRequest request) {
	}

	void response(HttpServletResponse response) {
	}
}
