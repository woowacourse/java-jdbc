package nextstep.mvc.view;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonViewTest {

    @DisplayName("model에 데이터가 1개일 때 기본 타입 데이터를 JSON 형식으로 반환한다.")
    @Test
    void renderWhenOneBasicData() throws Exception {
        // given
        final var view = new JsonView();
        final var model = Map.of("user", "박채영");

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var writer = mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writer);

        // when
        view.render(model, request, response);

        // then
        verify(writer).write("\"박채영\"");
    }

    @DisplayName("model에 데이터가 1개일 때 커트텀 객체 데이터를 JSON 형식으로 반환한다.")
    @Test
    void renderWhenOneCustomClass() throws Exception {
        // given
        final var view = new JsonView();
        final var model = Map.of(
                "user", new TestClass(1L, "라리사", new TestInnerClass(true, 10_000))
        );

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var writer = mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writer);

        // when
        view.render(model, request, response);

        // then
        verify(writer).write("{\"id\":1,\"name\":\"라리사\",\"innerClass\":{\"payed\":true,\"amount\":10000}}");
    }

    @DisplayName("model에 데이터가 0개면 빈 문자열을 반환한다.")
    @Test
    void renderWhenNoData() throws Exception {
        // given
        final var view = new JsonView();
        final var model = new HashMap<String, Object>();

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var writer = mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writer);

        // when
        view.render(model, request, response);

        // then
        verify(writer).write("");
    }

    @DisplayName("model에 데이터가 여러개면 Map 그대로 JSON으로 변환해 반환한다.")
    @Test
    void renderWhenMultiData() throws IOException {
        // given
        final var view = new JsonView();
        final var model = new LinkedHashMap<String, Object>();
        model.put("user1", "김지수");
        model.put("user2", new TestClass(2L, "김제니"));

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var writer = mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writer);

        // when
        view.render(model, request, response);

        // then
        verify(writer).write("{\"user1\":\"김지수\",\"user2\":{\"id\":2,\"name\":\"김제니\",\"innerClass\":null}}");
    }

    private static class TestClass {

        private Long id;
        private String name;
        private TestInnerClass innerClass;

        public TestClass(final Long id, final String name) {
            this(id, name, null);
        }

        public TestClass(final Long id, final String name, final TestInnerClass innerClass) {
            this.id = id;
            this.name = name;
            this.innerClass = innerClass;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public TestInnerClass getInnerClass() {
            return innerClass;
        }
    }

    private static class TestInnerClass {

        private boolean payed;
        private int amount;

        public TestInnerClass(final boolean payed, final int amount) {
            this.payed = payed;
            this.amount = amount;
        }

        public boolean isPayed() {
            return payed;
        }

        public int getAmount() {
            return amount;
        }
    }
}
