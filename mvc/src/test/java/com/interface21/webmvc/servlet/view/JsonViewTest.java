package com.interface21.webmvc.servlet.view;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.web.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import samples.User;

public class JsonViewTest {

    private JsonView jsonView;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;

    @BeforeEach
    public void setUp() throws IOException {
        jsonView = new JsonView();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void model에_데이터가_1개일_떄_값을_그대로_반환한다() throws Exception {
        // given
        User user = new User(1L, "dora", "password", "dora@example.com");
        Map<String, Object> model = Collections.singletonMap("user", user);

        // when
        jsonView.render(model, request, response);

        // then
        String expectedJson = """
                {
                    "id": 1,
                    "account": "dora",
                    "password": "password",
                    "email": "dora@example.com"
                }
                """;
        JSONAssert.assertEquals(expectedJson, stringWriter.toString(), false);
        verify(response).setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    @Test
    public void model에_데이터가_2개_이상이면_Map_형태_그대로_JSON으로_변환해서_반환한다() throws Exception {
        // given
        User user = new User(1L, "dora", "password", "dora@example.com");
        User admin = new User(2L, "admin", "password", "admin@example.com");
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        model.put("admin", admin);

        // when
        jsonView.render(model, request, response);

        // Then
        String expectedJson = """
                {
                    "user": {
                        "id": 1,
                        "account": "dora",
                        "password": "password",
                        "email": "dora@example.com"
                    },
                    "admin": {
                        "id": 2,
                        "account": "admin",
                        "password": "password",
                        "email": "admin@example.com"
                    }
                }
                """;
        JSONAssert.assertEquals(expectedJson, stringWriter.toString(), false);
    }
}
