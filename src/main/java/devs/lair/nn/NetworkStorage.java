package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class NetworkStorage {

    private NetworkStorage() {
        throw new UnsupportedOperationException();
    }

    public static NeuralNetwork loadFromFile(@NotNull Path path) {
        Checker.checkFile(path);
        try {
            List<String> lines = Files.readAllLines(path);

            if (lines.size() != 6) {
                throw new IllegalArgumentException("Wrong input file");
            }

            int inputNodesNumber = Integer.parseInt(lines.getFirst());
            int hiddenNodesNumber = Integer.parseInt(lines.get(1));
            int outputNodesNumber = Integer.parseInt(lines.get(2));
            double learningRate = Double.parseDouble(lines.get(3));

            NeuralNetwork nn = new NeuralNetwork(inputNodesNumber,
                    hiddenNodesNumber, outputNodesNumber, learningRate);

            double[][] ihw = createArrayFromString(lines.get(4), hiddenNodesNumber, inputNodesNumber);
            double[][] how = createArrayFromString(lines.get(5), outputNodesNumber, hiddenNodesNumber);

            nn.initWeights(ihw, how);
            return nn;
        } catch (Exception e) {
            throw new IllegalStateException("Can not load from file", e);
        }
    }

    private static double[][] createArrayFromString(@NotNull String line, int rowsCount, int colCount) {
        String[] split = line.split(",");
        Checker.checkSplit(split);

        if (split.length != rowsCount * colCount) {
            throw new IllegalArgumentException("Wrong line from file");
        }

        double[][] matrix = new double[rowsCount][colCount];
        for (int row = 0; row < matrix.length; row++) {
            for (int column = 0; column < matrix[0].length; column++) {
                matrix[row][column] = Double.parseDouble(split[row * colCount + column]);
            }
        }

        return matrix;
    }

    public static @NotNull File saveToFile(@NotNull NeuralNetwork nn, @NotNull Path path) {
        File file = new File(path.toString());
        try {
            if (!file.exists()) {
                Files.createFile(path);
            }

            List<String> outlines = new ArrayList<>();
            outlines.add(String.valueOf(nn.getInputNodesNumber()));
            outlines.add(String.valueOf(nn.getHiddenNodesNumber()));
            outlines.add(String.valueOf(nn.getOutputNodesNumber()));
            outlines.add(String.valueOf(nn.getLearningRate()));
            outlines.add(MatrixUtils.toString(nn.getInputToHiddenWeights()));
            outlines.add(MatrixUtils.toString(nn.getHiddenToOutputsWeights()));
            Files.write(path, outlines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Can not save to file", e);
        }

        return file;
    }

}
