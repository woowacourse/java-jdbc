package com.techcourse.service;

import com.techcourse.domain.User;

/**
 * 사용자 서비스 인터페이스
 * <p>
 * 비즈니스 로직과 트랜잭션 처리를 분리하기 위한 인터페이스
 * </p>
 */
public interface UserService {

    User findById(long id);

    void insert(User user);

    void changePassword(long id, String newPassword, String createdBy);
}
