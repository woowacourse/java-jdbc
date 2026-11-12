package com.techcourse.service;

import java.sql.SQLException;

public class PasswordChangeCommittedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PasswordChangeCommittedException(final SQLException cause) {
        super("비밀번호 변경과 이력 저장을 커밋한 뒤 연결을 닫는 데 실패했습니다.", cause);
    }
}
