package nextstep.mvc.view;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JsonViewTest {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private PrintWriter writer;

	@BeforeEach
	void init() throws IOException {
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		writer = mock(PrintWriter.class);

		given(response.getWriter())
			.willReturn(writer);
	}

	@DisplayName("model에 값이 1개일 때 json을 출력한다.")
	@Test
	void render_one() throws Exception {
		// given
		View view = new JsonView();
		Map<String, ?> model = Map.of("data", new Data("nameValue"));

		// when
		view.render(model, request, response);

		// then
		verify(writer).write("{\"name\":\"nameValue\"}");
	}

	@DisplayName("model에 값이 여러 개일 때 json을 출력한다.")
	@Test
	void render_multi() throws Exception {
		// given
		View view = new JsonView();
		Map<String, Data> model = new LinkedHashMap<>();
		model.put("data1", new Data("nameValue1"));
		model.put("data2", new Data("nameValue2"));
		model.put("data3", new Data("nameValue3"));

		// when
		view.render(model, request, response);

		// then
		verify(writer).write(
			"{\"data1\":{\"name\":\"nameValue1\"},"
				+ "\"data2\":{\"name\":\"nameValue2\"},"
				+ "\"data3\":{\"name\":\"nameValue3\"}}"
		);
	}

	static class Data {
		private String name;

		public Data(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
