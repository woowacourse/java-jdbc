package com.techcourse.domain;

import java.time.LocalDateTime;

public class UserHistory {

    private Long id;

    private final long userId;
    private final String account;
    private final String password;
    private final String email;

    private final LocalDateTime createdAt;

    private final String createBy;

    public UserHistory(final User user, final String createBy) {
        this(null, user.getId(), user.getAccount(), user.getPassword(), user.getEmail(), createBy);
    }

    public UserHistory(Long id, long userId, String account, String password, String email, String createBy) {
        this(id, userId, account, password, email, LocalDateTime.now(), createBy);
    }

    public UserHistory(
            Long id,
            long userId,
            String account,
            String password,
            String email,
            LocalDateTime createdAt,
            String createBy
    ) {
        this.id = id;
        this.userId = userId;
        this.account = account;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.createBy = createBy;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreateBy() {
        return createBy;
    }
}
