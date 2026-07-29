package devs.lair.nn;

import devs.lair.nn.ui.MnistCsvViewer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

class NeuralNetworkTrainTest {

    @DisplayName("Train on 100 numbers")
    @RepeatedTest(10)
    void trainOn100() throws IOException {
        URL defaultUrl = MnistCsvViewer.class.getResource("/mnist/mnist_train_100.csv");
        assertThat(defaultUrl).isNotNull();

        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);
        for (int i = 0; i < 1; i++) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(defaultUrl.getFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    double[] target = converNumberToTargetArray(Integer.parseInt(split[0]));
                    double[] inputs = convertLineToInputArray(split);
                    nn.train(inputs, target);
                }
            }
        }

        URL testUrl = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        assertThat(testUrl).isNotNull();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(testUrl.getFile()))) {
            String line;
            double performanceRate;
            int totalLines = 0;
            int correctLine = 0;
            while ((line = reader.readLine()) != null ) {
                totalLines++;
                String[] split = line.split(",");

                double[] inputs = convertLineToInputArray(split);
                double[][] output = nn.query(inputs);

                int answer = extractFromOutputs(output);
                int number = Integer.parseInt(split[0]);

                if (answer == number) {
                    correctLine++;
                }
            }

            performanceRate = correctLine / (double) totalLines;
            System.out.println(performanceRate);
        }
    }

    private int extractFromOutputs(double[][] output) {
        double max = Double.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < output.length; i++) {
            double current = output[i][0];
            if (current > max) {
                max = current;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private double[] convertLineToInputArray(String[] split) {
        double[] result = new double[split.length - 1];
        for (int i = 1; i < split.length ; i++) {
            result[i - 1] = ((double) Integer.parseInt(split[i]) / 255) * 0.99 + 0.01;
        }
        return result;
    }

    private double[] converNumberToTargetArray(int number) {
        double[] array = new double[] {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01};
        array[number] = 0.99;
        return array;
    }

}
