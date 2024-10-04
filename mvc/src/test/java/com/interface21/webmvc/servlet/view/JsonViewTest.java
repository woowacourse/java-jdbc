package com.interface21.webmvc.servlet.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JsonViewTest {

    private final JsonView jsonView = new JsonView();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("model에 데이터가 1개이면 해당 값의 객체만 Json으로 변환하여 반환한다.")
    @Test
    void returnOneObject() throws Exception {
        Object user = new User("gugu", "1234");
        Map<String, ?> model = Map.of("user", user);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        jsonView.render(model, mock(HttpServletRequest.class), response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(outputStream).print(captor.capture());

        assertThat(captor.getValue()).isEqualTo("{\"account\":\"gugu\",\"password\":\"1234\"}");
    }

    @DisplayName("model에 데이터가 2개 이상이면 Map형태를 Json으로 변환하여 반환한다.")
    @Test
    void returnMap() throws Exception {
        Map<String, ?> model = Map.of(
                "account", "gugu",
                "password", "1234"
        );

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        jsonView.render(model, mock(HttpServletRequest.class), response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(outputStream).print(captor.capture());

        Map<String, Object> actualMap = objectMapper.readValue(captor.getValue(), Map.class);
        Map<String, Object> expectedMap = objectMapper.readValue("{\"account\":\"gugu\",\"password\":\"1234\"}", Map.class);
        assertThat(actualMap).isEqualTo(expectedMap);
    }

    private static class User {
        private final String account;
        private final String password;

        public User(String account, String password) {
            this.account = account;
            this.password = password;
        }
    }
}
