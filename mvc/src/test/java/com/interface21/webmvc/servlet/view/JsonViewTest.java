package com.interface21.webmvc.servlet.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonViewTest {

    @Test
    @DisplayName("모델의 개수가 한 개인 경우, 값 하나를 그대로 반환한다.")
    void convertModelTest() throws IOException {
        JsonView view = new JsonView();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true);
        when(response.getWriter()).thenReturn(writer);
        view.render(Map.of("account", "테니"), request, response);

        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("\"테니\"");
    }

    @Test
    @DisplayName("모델의 개수가 여러 개인 경우, 모델을 그대로 반환한다.")
    void convertModelMapTest() throws IOException {
        JsonView view = new JsonView();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true);
        when(response.getWriter()).thenReturn(writer);
        view.render(Map.of("account", "테니", "course", "backend"), request, response);

        assertThat(out.toString(StandardCharsets.UTF_8))
                .contains("\"account\":\"테니\"")
                .contains("\"course\":\"backend\"");
    }
}
