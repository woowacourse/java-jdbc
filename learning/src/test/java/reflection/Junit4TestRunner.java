package reflection;

import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class Junit4TestRunner extends OutputTest {

    @Test
    void run() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        // given
        Class<Junit4Test> clazz = Junit4Test.class;
        Junit4Test junit4Test = clazz.getConstructor().newInstance();
        Method[] methods = clazz.getMethods();

        // when
        for (Method method : methods) {
            if (method.isAnnotationPresent(MyTest.class)) {
                method.invoke(junit4Test);
            }
        }

        // then
        String output = captor.toString().trim();
        assertThat(output)
                .contains(
                        "Running Test1",
                        "Running Test2"
                ).doesNotContain(
                        "Running Test3"
                );
    }

    @Test
    void run2() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        // given
        Class<Junit4Test> clazz = Junit4Test.class;
        Junit4Test junit4Test = clazz.getConstructor().newInstance();
        Set<Method> methods = ReflectionUtils.getMethods(clazz, method -> method.isAnnotationPresent(MyTest.class));

        // when
        for (Method method : methods) {
            method.invoke(junit4Test);
        }

        // then
        String output = captor.toString().trim();
        assertThat(output)
                .contains(
                        "Running Test1",
                        "Running Test2"
                ).doesNotContain(
                        "Running Test3"
                );
    }
}
