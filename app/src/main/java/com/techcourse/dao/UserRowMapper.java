package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapForObject(final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
        }
        throw new IllegalStateException("쿼리 실행 결과 기반 인스턴스 생성에 실패했습니다");
    }

    @Override
    public List<User> mapForObjects(final ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            final User user = new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            users.add(user);
        }
        return users;
    }
}
