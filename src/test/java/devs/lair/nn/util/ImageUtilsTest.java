package devs.lair.nn.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUtilsTest {

    @Test
    @DisplayName("Private constructor call (coverage test")
    void callPrivateConstructorTest() throws NoSuchMethodException {
        Constructor<ImageUtils> pcc = ImageUtils.class.getDeclaredConstructor();
        pcc.setAccessible(true);

        assertThatThrownBy(pcc::newInstance)
                .isInstanceOf(InvocationTargetException.class);

    }
}