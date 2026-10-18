package devs.lair.nn;

import devs.lair.nn.util.DiffUtils;
import devs.lair.nn.util.HuMoments;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RecognizeHuMomentsService implements INeuralNetwork {

    double[][] huMoments = new double[10][7];

    public RecognizeHuMomentsService() {
        initHuMoments();
    }

    private void initHuMoments() {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("mnist-mean-hu.csv"))) {
            String line;
            int n = 0;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                huMoments[n] = readHuMoments(split);
                n++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not read file: %s".formatted(e.getMessage()));
        }
    }

    private double[] readHuMoments(@NotNull String[] split) {
        double[] result = new double[7];
        for (int i = 0; i < split.length; i++) {
            result[i] = Double.parseDouble(split[i]);
        }

        return result;
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
        int[][] intensive = HuMoments.getIntensive(inputs);
        double[] inputHuMoments = HuMoments.hu(intensive);

        double[] diffs = new double[10];
        for (int i = 0; i < huMoments.length; i++) {
            diffs[i] = DiffUtils.calculateEuclidDistance(huMoments[i], inputHuMoments);
        }

        return transformToResult(diffs);
    }

    private double[][] transformToResult(double[] diffs) {
        double[][] result = new double[10][1];

        double minDiff = DiffUtils.min(diffs);
        for (int i = 0; i < diffs.length; i++) {
            result[i][0] = 1 - (Math.abs(minDiff - diffs[i]) / diffs[i]);
        }

        return result;
    }

    private static int[] convertLineToInputArray(String[] split) {
        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }
}
