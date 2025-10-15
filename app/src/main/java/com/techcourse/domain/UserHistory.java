package com.techcourse.domain;

import java.time.LocalDateTime;

import com.interface21.jdbc.core.jpa.annotation.Column;
import com.interface21.jdbc.core.jpa.annotation.Id;
import com.interface21.jdbc.core.jpa.annotation.Table;

@Table(name = "user_history")
public class UserHistory {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private final long userId;

    @Column(name = "account")
    private final String account;

    @Column(name = "password")
    private final String password;

    @Column(name = "email")
    private final String email;

    private final LocalDateTime createdAt;

    private final String createBy;

    public UserHistory(final User user, final String createBy) {
        this(null, user.getId(), user.getAccount(), user.getPassword(), user.getEmail(), createBy);
    }

    public UserHistory(final Long id, final long userId, final String account, final String password, final String email, final String createBy) {
        this.id = id;
        this.userId = userId;
        this.account = account;
        this.password = password;
        this.email = email;
        this.createdAt = LocalDateTime.now();
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
