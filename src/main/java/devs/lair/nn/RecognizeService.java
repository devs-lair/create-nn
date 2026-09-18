package devs.lair.nn;

import devs.lair.nn.util.DiffUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RecognizeService implements INeuralNetwork {
    Map<Integer, List<Probability>> probabilities = new HashMap<>();
    int[][] meanColor = new int[10][];

    public RecognizeService() {
        loadProbabilities();
    }

    private void loadProbabilities() {
        for (int n = 0; n < 10; n++) {
            List<int[]> records = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(Path.of("mnist-common-" + n + ".csv"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    records.add(convertLineToInputArray(split));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Can not read file: %s".formatted(e.getMessage()));
            }

            int[] colorProb = records.getFirst();
            int[] countProb = records.get(1);
            int number = colorProb[0];
            List<Probability> numberProbabilities = new ArrayList<>();

            for (int i = 1; i < records.getFirst().length; i++) {
                numberProbabilities.add(new Probability(countProb[i], (colorProb[i] / 255.0) * 0.99 + 0.01));

            }

            meanColor[n] = new int[colorProb.length - 1];
            System.arraycopy(colorProb,1, meanColor[n], 0, colorProb.length -1);

            probabilities.put(number, numberProbabilities);
        }
    }

    @Override
    public void train(@NotNull List<TrainRecord> batch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void train(double[] inputs, double[] targets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[][] query(double[] inputs) {
        int[] diffs = new int[10];
        for (int i = 0; i < meanColor.length; i++) {
            diffs[i] = calculateDiff(inputs, meanColor[i]);
        }

        return transformToResult(diffs);
    }

    private double[][] transformToResult(int[] diffs) {
        double[][] result = new double[10][1];

        int minDiff = DiffUtils.min(diffs);
        for (int i = 0; i < diffs.length; i++) {
            result[i][0] = 1 - ((double) Math.abs(minDiff - diffs[i]) / diffs[i]);
        }

        return result;
    }

    private int calculateDiff(double[] inputs, int[] meanColor) {
        int[] inputColor = new int[inputs.length];

        for (int i = 0; i < inputColor.length; i++) {
            inputColor[i] = (int) (((inputs[i] - 0.01) * 255) / 0.99);
        }
        return DiffUtils.calculateEuclidDistance(meanColor, inputColor);
    }

    private static int[] convertLineToInputArray(String[] split) {
        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }

    private record Probability(double countProb, double colorProb) {

    }
}
