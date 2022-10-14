package nextstep.jdbc;

import java.lang.reflect.Field;

class KeyHose {

    public void injectKey(KeyHolder keyHolder, Long key) {
        try {
            Field field = keyHolder.getClass().getDeclaredField("generatedKey");
            field.setAccessible(true);
            field.set(keyHolder, key);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
