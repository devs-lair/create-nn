package devs.lair.nn.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class CheckerTest {

    @Test
    void checkFileNegative() {
        Path wrongPath = Paths.get("-__@_-.cv");

        assertThatThrownBy(() -> Checker.checkFile(wrongPath));

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(()->Files.exists(wrongPath)).thenReturn(true);
            assertThatThrownBy(() -> Checker.checkFile(wrongPath))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("File is empty");

            files.when(()->Files.size(wrongPath)).thenThrow(new IOException());
            assertThatThrownBy(() -> Checker.checkFile(wrongPath))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Wrong csv file");
        }
    }

    @Test
    void checkSplit() {
        assertThatThrownBy(() -> Checker.checkSplit(new String[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkCsvSplit() {
        assertThatThrownBy(() -> Checker.checkCsvSplit(new String[0]))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Checker.checkCsvSplit(new String[] {"1", "2"}))
                .isInstanceOf(IllegalArgumentException.class);

        assertDoesNotThrow(()->Checker.checkCsvSplit(new String[] {"1", "2", "3"}));
    }

    @Test
    @DisplayName("Private constructor call (coverage test")
    void callPrivateConstructorTest() throws NoSuchMethodException {
        Constructor<Checker> pcc = Checker.class.getDeclaredConstructor();
        pcc.setAccessible(true);

        assertThatThrownBy(pcc::newInstance)
                .isInstanceOf(InvocationTargetException.class);

    }
}