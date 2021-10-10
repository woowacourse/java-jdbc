package reflection;

import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class Junit3TestRunner extends OutputTest {

    @Test
    void run() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        // given
        Class<Junit3Test> clazz = Junit3Test.class;
        Junit3Test junit3Test = clazz.getConstructor().newInstance();
        Method[] methods = clazz.getMethods();

        // when
        for (Method method : methods) {
            if (method.getName().startsWith("test")) {
                method.invoke(junit3Test);
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
        Class<Junit3Test> clazz = Junit3Test.class;
        Junit3Test junit3Test = clazz.getConstructor().newInstance();
        Set<Method> methods = ReflectionUtils.getMethods(clazz, method -> method.getName().startsWith("test"));

        // when
        for (Method method : methods) {
            method.invoke(junit3Test);
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
