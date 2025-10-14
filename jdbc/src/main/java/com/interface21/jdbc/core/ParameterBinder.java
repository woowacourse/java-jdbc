package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public class ParameterBinder {

    private final Map<Class<?>, TypeBinder> typeBinders;

    public static ParameterBinder init() {
        return new ParameterBinder(
                Map.ofEntries(
                        Map.entry(Integer.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setInt(index, (Integer) parameter)),

                        Map.entry(Long.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setLong(index, (Long) parameter)),

                        Map.entry(Boolean.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setBoolean(index, (Boolean) parameter)),

                        Map.entry(Float.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setFloat(index, (Float) parameter)),

                        Map.entry(Double.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setDouble(index, (Double) parameter)),

                        Map.entry(String.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setString(index, (String) parameter)),

                        Map.entry(LocalDate.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setDate(index, Date.valueOf((LocalDate) parameter))),

                        Map.entry(LocalTime.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setTime(index, Time.valueOf((LocalTime) parameter))),

                        Map.entry(LocalDateTime.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setTimestamp(index, Timestamp.valueOf((LocalDateTime) parameter))),

                        Map.entry(Date.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setDate(index, (Date) parameter)),

                        Map.entry(Time.class, (preparedStatement, index, parameter)
                                -> preparedStatement.setTime(index, (Time) parameter))
                )
        );
    }

    private ParameterBinder(Map<Class<?>, TypeBinder> typeBinders) {
        this.typeBinders = typeBinders;
    }

    public void bindParameter(
            final PreparedStatement preparedStatement,
            final int index,
            final Object parameter
    ) throws SQLException {
        if (parameter == null) {
            setNull(preparedStatement, index);
            return;
        }

        final TypeBinder typeBinder = typeBinders.get(parameter.getClass());
        if (typeBinder == null) {
            throw new DataAccessException("지원하지 않는 자료형입니다.: " + parameter.getClass());
        }
        typeBinder.bind(preparedStatement, index, parameter);
    }

    private void setNull(
            final PreparedStatement preparedStatement,
            final int index
    ) throws SQLException {
        preparedStatement.setNull(index, Types.OTHER);
    }
}
