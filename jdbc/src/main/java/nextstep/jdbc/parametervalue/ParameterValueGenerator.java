package nextstep.jdbc.parametervalue;

import nextstep.jdbc.exception.NotSupportedSqlTypeException;

import java.util.Arrays;
import java.util.function.Function;

public enum ParameterValueGenerator {

    BYTE(Byte.class, ByteParameterValue::new),
    BOOLEAN(Boolean.class, BooleanParameterValue::new),
    SHORT(Short.class, ShortParameterValue::new),
    INT(Integer.class, IntegerParameterValue::new),
    FLOAT(Float.class, FloatParameterValue::new),
    LONG(Long.class, LongParameterValue::new),
    DOUBLE(Double.class, DoubleParameterValue::new),
    STRING(String.class, StringParameterValue::new);

    private final Class<?> ParameterType;
    private final Function<Object, ParameterValue> function;

    ParameterValueGenerator(Class<?> parameterType, Function<Object, ParameterValue> function) {
        ParameterType = parameterType;
        this.function = function;
    }

    public static ParameterValue createParameterValue(Object value) {
        return Arrays.stream(values())
                .filter(parameterValue -> parameterValue.ParameterType.isInstance(value))
                .map(parameterValue -> parameterValue.function.apply(value))
                .findAny()
                .orElseThrow(() -> new NotSupportedSqlTypeException("Not Supported Type SQL Parameter Value"));
    }
}
