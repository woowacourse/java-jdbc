package com.techcourse.repository;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper implements JdbcMapper<User> {

    @Override
    public List<User> mapRow(ResultSet resultSet) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<String> fieldNames = Arrays.stream(User.class.getDeclaredFields())
                .map(Field::getName)
                .filter(each -> !each.equals("id"))
                .collect(Collectors.toUnmodifiableList());
        Constructor<User> constructor = User.class.getConstructor(String.class, String.class, String.class);
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> result = new ArrayList<>();
            for (String name : fieldNames) {
                result.add(resultSet.getObject(name));
            }
            users.add(constructor.newInstance(result.toArray()));
        }
        return users;
    }
}
