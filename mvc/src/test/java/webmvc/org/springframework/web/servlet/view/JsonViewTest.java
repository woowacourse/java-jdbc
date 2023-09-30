package webmvc.org.springframework.web.servlet.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JsonViewTest {

    @Test
    void Model_에_맞는_JSON_을_반환할_수_있다() throws Exception {
        // given
        Map<String, String> model = new LinkedHashMap<>();
        model.put("key", "value");

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        PrintWriter printWriter = Mockito.mock(PrintWriter.class);
        when(response.getWriter())
                .thenReturn(printWriter);

        // when
        JsonView jsonView = new JsonView();
        jsonView.render(model, request, response);

        // then
        verify(printWriter).write("{\"key\":\"value\"}");
    }
}
