package devs.lair.nn;

import devs.lair.nn.ui.MnistCsvViewer;
import devs.lair.nn.util.HuMoments;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RecognizeHuMomentsServiceTest {

    @Test
    @DisplayName("Try to recognize")
    void recognizeServiceTest() {
        INeuralNetwork recognizeService = new RecognizeHuMomentsService();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(recognizeService,
                Paths.get(validateFile.getFile())).getPerformance();

        assertThat(performance).isGreaterThan(0.9);
        System.out.println(performance);
    }

    @Test
    @DisplayName("Hu moments of same")
    void sameHuMoments() throws IOException {
        List<MeanHuMoment> statByNumber = new ArrayList<>();
        for (int i = 0; i < 10 ; i++) {
            try (BufferedReader reader = Files.newBufferedReader(Path.of("mnist-sorted-euclid-distance-"+i+".csv"))) {
                MeanHuMoment meanHuMoment = new MeanHuMoment();
                statByNumber.add(meanHuMoment);

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    int[] ints = convertLineToInputArray(split);
                    double[] normalize = HuMoments.normalize(ints);
                    int[][] intensive = HuMoments.getIntensive(normalize);
                    double[] hu = HuMoments.hu(intensive);
                    meanHuMoment.proceedMoments(hu);
                }


            } catch (IOException e) {
                throw new IllegalArgumentException("Can not read file: %s".formatted(e.getMessage()));
            }
        }

        List<String> means = new ArrayList<>();
        for (MeanHuMoment meanHuMoment : statByNumber) {
            StringBuilder numberMean = new StringBuilder();
            for (MomentStat momentStat : meanHuMoment.getMomentStats()) {
                numberMean.append(momentStat.getMean()).append(",");
            }
            numberMean.delete(numberMean.length() - 1, numberMean.length());
            means.add(numberMean.toString());
        }


        File forSave = new File("mnist-mean-hu.csv");

        if (!forSave.exists()) {
            Files.createFile(forSave.toPath());
        }

        Files.write(forSave.toPath(),
                means,
                StandardOpenOption.TRUNCATE_EXISTING);
    }


    private static int[] convertLineToInputArray(@NotNull String[] split) {
        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }

    private static class MeanHuMoment {
        private final List<MomentStat> momentStats = List.of(new MomentStat(), new MomentStat(), new MomentStat(),
                new MomentStat(), new MomentStat(), new MomentStat(), new MomentStat());


        public void proceedMoments(double[] hu) {
            for (int i = 0; i < hu.length; i++) {
                MomentStat momentStat = momentStats.get(i);
                momentStat.setMax(hu[i]);
                momentStat.setMin(hu[i]);
                momentStat.setMean(hu[i]);
            }
        }

        public List<MomentStat> getMomentStats() {
            return momentStats;
        }
    }

    private static class MomentStat {
        double max;
        double min;
        double mean;

        public MomentStat() {
            this.max = Double.MIN_VALUE;
            this.min = Double.MAX_VALUE;
            this.mean = 0;
        }

        public MomentStat(double max, double min) {
            this.max = max;
            this.min = min;
        }

        public void setMax(double newMax) {
            if (newMax > this.max) {
                this.max = newMax;
            }
        }

        public void setMin(double newMin) {
            if (newMin < this.min) {
                this.min = newMin;
            }
        }

        public double getMax() {
            return max;
        }

        public double getMin() {
            return min;
        }

        public double getMean() {
            return mean;
        }

        public void setMean(double v) {
            if (Double.isNaN(v)) {
                return;
            }

            if (this.mean == 0) {
                this.mean = v;
            } else {
                this.mean = (this.mean + v) / 2;
            }
        }
    }
}
