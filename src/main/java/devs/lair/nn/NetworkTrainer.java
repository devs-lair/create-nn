package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NetworkTrainer {
    private static PrintStream out = System.out;

    private NetworkTrainer() {
        throw new UnsupportedOperationException();
    }

    public static void setPrintStream(@Nullable PrintStream out) {
        NetworkTrainer.out = out;
    }

    public static Duration trainNetwork(@NotNull NeuralNetwork nn, @NotNull Path csvFile) {
        return trainNetwork(nn, csvFile, 1);
    }

    public static Duration trainNetwork(@NotNull NeuralNetwork nn,
                                        @NotNull Path csvFile,
                                        int epochs) {
        Checker.checkFile(csvFile);

        double[][] targets = new double[nn.getOutputNodesNumber()][1];
        for (double[] row : targets) {
            row[0] = 0.01;
        }

        double[][] inputs = new double[nn.getInputNodesNumber()][1];

        Instant startTime = Instant.now();
        String line;

        int prevNumber = -1;
        int totalRecords = 0;

        for (int e = 0; e < epochs; e++) {
            if (out != null) {
                out.printf("Start epoch %d, start at %s%n", e + 1, new Date());
            }

            int epochRecords = 0;
            try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    Checker.checkSplit(split);

                    //prepare targets
                    int currentNumber = Integer.parseInt(split[0]);
                    targets[currentNumber][0] = 0.99;
                    if (prevNumber != -1) {
                        targets[prevNumber][0] = 0.01;
                    }
                    prevNumber = currentNumber;

                    //prepare inputs
                    for (int i = 0; i < inputs.length; i++) {
                        inputs[i][0] = ((double) Integer.parseInt(split[i+1]) / 255) * 0.99 + 0.01;
                    }

                    nn.train(inputs, targets);
                    epochRecords++;

                    if (epochRecords % 10000 == 0 && out != null) {
                        out.printf("Processed %d records %n", epochRecords);
                    }
                }
                totalRecords += epochRecords;
            } catch (IOException ex) {
                throw new IllegalStateException("Error while reading file: " + csvFile, ex);
            }
            if (out != null) {
                out.printf("End epoch %d, records processed %d, duration %s ms %n", e + 1,
                        epochRecords, Duration.between(startTime, Instant.now()).toMillis());
            }
        }

        Duration trainTime = Duration.between(startTime, Instant.now());
        if (out != null) {
            out.printf("Training finished, epochs %d, total records processed %d, " +
                            "duration %s ms %n",
                    epochs, totalRecords, trainTime.toMillis());
        }

        return trainTime;
    }

    public static ValidationReport validateNetwork(@NotNull NeuralNetwork nn,
                                                   @NotNull Path csvFile) {
        Checker.checkFile(csvFile);

        Instant startTime = Instant.now();
        if (out != null) {
            out.printf("Start validation at %s %n", new Date());
        }

        double performanceRate;
        int totalRecords = 0;
        int correctRecords = 0;
        List<String> wrongRecords = new ArrayList<>();
        double[][] inputs = new double[nn.getInputNodesNumber()][1];
        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalRecords++;
                String[] split = line.split(",");

                //prepare inputs
                for (int i = 0; i < inputs.length; i++) {
                    inputs[i][0] = ((double) Integer.parseInt(split[i+1]) / 255) * 0.99 + 0.01;
                }

                double[][] output = nn.query(inputs);
                int answer = getIndexOfMaxElementInOutputs(output);
                int number = Integer.parseInt(split[0]);

                if (answer == number) {
                    correctRecords++;
                } else {
                    wrongRecords.add(line);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading file: " + csvFile, e);
        }

        performanceRate = correctRecords / (double) totalRecords;
        Duration validateTime = Duration.between(startTime, Instant.now());

        if (out != null) {
            out.printf("Validation finished. Total records: %d, correct records: %d, " +
                            "performance rate: %f, validation time: %s ms%n",
                    totalRecords, correctRecords, performanceRate, validateTime.toMillis());
        }

        return new ValidationReport(totalRecords, correctRecords, wrongRecords, validateTime, csvFile, nn);
    }

    private static int getIndexOfMaxElementInOutputs(double[][] output) {
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

    private static double[][] convertLineToInputArray(double[][] inputs, @NotNull String[] split) {

        return inputs;
    }

//    private static double[][] converNumberToTargetArray(int number) {
//        double[] array = new double[]{0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01};
//        array[number] = 0.99;
//        return array;
//    }
}