package nextstep.mvc.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import nextstep.web.support.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("JsonView는")
class JsonViewTest {

    private JsonView jsonView;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        jsonView = new JsonView();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        doNothing().when(response).setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    @DisplayName("viewName 요청시 예외가 발생한다.")
    @Test
    void viewNameException() {
        // when, then
        assertThatThrownBy(() -> jsonView.getViewName()).isExactlyInstanceOf(IllegalStateException.class);
    }

    @DisplayName("model data가 1개라면 data 내부의 필드 정보만을 반환한다.")
    @ParameterizedTest
    @MethodSource("modelSingleDataParameters")
    void modelSingleData(Map<String, ?> model, String expectJson) throws IOException {
        // when
        jsonView.render(model, request, response);

        // then
        assertThat(stringWriter.toString()).isEqualTo(expectJson);
    }

    @DisplayName("model data가 2개 이상이라면 data 내부의 필드 정보만을 반환한다.")
    @ParameterizedTest
    @MethodSource("modelDataParameters")
    void modelData(Map<String, ?> model, String expectJson) throws IOException {
        // when
        jsonView.render(model, request, response);

        // then
        assertThat(stringWriter.toString()).isEqualTo(expectJson);
    }

    private static Stream<Arguments> modelSingleDataParameters() throws JsonProcessingException {
        Map<String, Object> 라이언 = new HashMap<>();
        Map<String, Object> 인비 = new HashMap<>();
        Map<String, Object> 검프 = new HashMap<>();
        Map<String, Object> 욘 = new HashMap<>();

        라이언.put("user", new User("라이언", "자는 중"));
        인비.put("user", new User("인비", "노래하는 중"));
        검프.put("user", new User("검프", "술 마시는 중"));
        욘.put("user", new User("욘", "음악하는 중"));

        ObjectMapper objectMapper = new ObjectMapper();

        return Stream.of(
            Arguments.of(라이언, objectMapper.writeValueAsString(라이언.get("user"))),
            Arguments.of(인비, objectMapper.writeValueAsString(인비.get("user"))),
            Arguments.of(검프, objectMapper.writeValueAsString(검프.get("user"))),
            Arguments.of(욘, objectMapper.writeValueAsString(욘.get("user")))
        );
    }

    private static Stream<Arguments> modelDataParameters() throws JsonProcessingException {
        Map<String, Object> 백중원_사무실 = new HashMap<>();

        백중원_사무실.put("user1", new User("라이언", "자는 중"));
        백중원_사무실.put("user2", new User("인비", "노래하는 중"));
        백중원_사무실.put("user3", new User("검프", "술 마시는 중"));
        백중원_사무실.put("user4", new User("욘", "음악하는 중"));

        Map<String, Object> 라이언네_집 = new HashMap<>();

        라이언네_집.put("user1", new User("라이언", "술 마시는 중"));
        라이언네_집.put("user2", new User("인비", "술 마시는 중"));
        라이언네_집.put("user3", new User("검프", "술 마시는 중"));
        라이언네_집.put("user4", new User("욘", "술 마시는 중"));

        ObjectMapper objectMapper = new ObjectMapper();

        return Stream.of(
            Arguments.of(백중원_사무실, objectMapper.writeValueAsString(백중원_사무실)),
            Arguments.of(라이언네_집, objectMapper.writeValueAsString(라이언네_집))
        );
    }

    private static class User {
        private final String name;
        private final String status;

        public User(String name, String status) {
            this.name = name;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }
    }
}