package devs.lair.nn.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Checker {

    public static void checkFile(@NotNull Path csvFile) {
        if (!Files.exists(csvFile)) {
            throw new IllegalArgumentException("File not exist");
        }

        try {
            if (Files.size(csvFile) == 0) {
                throw new IllegalArgumentException("File is empty");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Wrong csv file");
        }
    }

    public static void checkNodesNumbers(int inputNodesNumber,
                                   int hiddenNodesNumber,
                                   int outputNodesNumber) {
        if (inputNodesNumber < 1) {
            throw new IllegalArgumentException("Wrong inputs nodes number");
        }

        if (hiddenNodesNumber < 1) {
            throw new IllegalArgumentException("Wrong hidden nodes number");
        }

        if (outputNodesNumber < 1) {
            throw new IllegalArgumentException("Wrong output nodes number");
        }
    }

    public static void checkSplit(String[] split) {
        if (split.length == 0) {
            throw new IllegalArgumentException("Wrong split");
        }
    }
}
