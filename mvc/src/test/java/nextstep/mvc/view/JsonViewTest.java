package nextstep.mvc.view;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonViewTest {

    @DisplayName("model에 데이터가 하나이면 값을 바로 반환한다.")
    @Test
    void returnValueWhenOneData() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getWriter()).thenReturn(printWriter);

        final Map<String, Object> model = new HashMap<>();
        final Map<String, String> account = Map.of("account", "dwoo");
        model.put("account", account);

        final JsonView jsonView = new JsonView();
        jsonView.render(model, request, response);

        final String writtenValue = stringWriter.toString();
        assertThat(writtenValue).isEqualTo(objectMapper.writeValueAsString(account));
    }

    @DisplayName("데이터가 2개 이상이면 Map 형태 그대로 JSON 으로 변환해서 반환한다.")
    @Test
    void returnMapGreaterThenOwnData() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getWriter()).thenReturn(printWriter);

        final Map<String, Object> model = new HashMap<>();
        final Map<String, String> account = Map.of("account", "dwoo");
        final Map<String, String> password = Map.of("password", "password");
        model.put("account", account);
        model.put("password", password);

        final JsonView jsonView = new JsonView();
        jsonView.render(model, request, response);

        final String writtenValue = stringWriter.toString();
        final String expected = objectMapper.writeValueAsString(model);
        assertThat(writtenValue).isEqualTo(expected);
    }
}
