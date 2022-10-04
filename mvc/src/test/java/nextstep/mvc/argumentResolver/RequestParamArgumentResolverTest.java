package nextstep.mvc.argumentResolver;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.web.annotation.RequestParam;

class RequestParamArgumentResolverTest {

	@DisplayName("@RequestParam 파라미터를 처리할 수 있는지 판단한다")
	@Test
	void support_true() throws NoSuchMethodException {
		// given
		ArgumentResolver resolver = new RequestParamArgumentResolver();
		Method method = getClass().getDeclaredMethod("requestParam", String.class, String.class);

		// when
		boolean actual = resolver.support(method.getParameters()[0]);

		// then
		assertThat(actual).isTrue();
	}

	@DisplayName("@RequestParam이 없으면 처리할 수 없다")
	@Test
	void support_false() throws NoSuchMethodException {
		// given
		ArgumentResolver resolver = new RequestParamArgumentResolver();
		Method method = getClass().getDeclaredMethod("requestParam", String.class, String.class);

		// when
		boolean actual = resolver.support(method.getParameters()[1]);

		// then
		assertThat(actual).isFalse();
	}

	@DisplayName("@RequestParam 파라미터를 처리할 수 있다")
	@Test
	void resolve() throws NoSuchMethodException {
		// given
		ArgumentResolver resolver = new RequestParamArgumentResolver();
		Method method = getClass().getDeclaredMethod("requestParam", String.class, String.class);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		given(request.getParameter("value"))
			.willReturn("value1");

		// when
		Object actual = resolver.resolve(request, response, method.getParameters()[0]);

		// then
		assertThat((String)actual).isEqualTo("value1");
	}

	void requestParam(@RequestParam(name = "value") String value1, String value2) {
	}
}
