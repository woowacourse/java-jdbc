package com.techcourse.controller.request;

import jakarta.servlet.http.HttpSession;

public class LoginRequest {

    private final String account;
    private final String password;
    private final HttpSession httpSession;

    public LoginRequest(String account, String password, HttpSession httpSession) {
        this.account = account;
        this.password = password;
        this.httpSession = httpSession;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }
}
