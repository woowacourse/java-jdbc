package com.techcourse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    @DisplayName("실제 JSON 응답은 id·account·email만 포함하고 비밀번호를 노출하지 않는다")
    void renderedUserResponseContainsOnlyPublicFields() throws Exception {
        final var userDao = mock(UserDao.class);
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var output = new ByteArrayOutputStream();
        final var secret = "secret-must-never-be-exposed";
        when(request.getParameter("account")).thenReturn("alice");
        when(userDao.findByAccount("alice"))
                .thenReturn(new User(17L, "alice", secret, "alice@example.com"));
        when(response.getOutputStream()).thenReturn(captureOutput(output));

        final var result = new UserController(userDao).show(request, response);
        result.getView().render(result.getModel(), request, response);

        final var body = output.toString(StandardCharsets.UTF_8);
        final var json = new ObjectMapper().readTree(body);
        final var keys = new HashSet<String>();
        json.fieldNames().forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder("id", "account", "email");
        assertThat(json.get("id").asLong()).isEqualTo(17L);
        assertThat(json.get("account").asText()).isEqualTo("alice");
        assertThat(json.get("email").asText()).isEqualTo("alice@example.com");
        assertThat(body).doesNotContain(secret);
    }

    private ServletOutputStream captureOutput(final ByteArrayOutputStream output) {
        return new ServletOutputStream() {
            @Override
            public void write(final int value) {
                output.write(value);
            }

            @Override
            public void write(final byte[] bytes, final int offset, final int length) {
                output.write(bytes, offset, length);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(final WriteListener listener) {
            }
        };
    }
}
