package devs.lair.nn;

import devs.lair.nn.ui.MnistCsvViewer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkTrainerTest {

    @BeforeEach
    void beforeEach() {
        MatrixUtils.setNoChecks(true);
        NetworkTrainer.setPrintStream(null);
    }

    @AfterEach
    void AfterEach() {
        MatrixUtils.setNoChecks(false);
        NetworkTrainer.setPrintStream(System.out);
    }

    @Test
    @DisplayName("Train 100 records and validate")
    public void train100records() {
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        //train
        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train_100.csv");
        assertThat(trainFile).isNotNull();
        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()));
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.45);
    }

    @Test
    @DisplayName("Train 100 records with 5 epochs and validate")
    public void train100records5epochs() {
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train_100.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()), 5);
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("Train 10000 records")
    @Disabled("Long running test")
    @Tag("slow")
    public void train10000records() {
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()));
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.8);
    }

    @Test
    @DisplayName("Train 60000 records")
    @Disabled("Long running test")
    @Tag("slow")
    public void train60000records() {
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()));
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.9);
    }

    @Test
    @DisplayName("Train 10000 records, 5 epochs")
    @Disabled("Long running test")
    @Tag("slow")
    public void train10000and5epochs() {
        //NetworkTrainer.setPrintStream(System.out);
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()), 5);
        assertThat(duration).isNotNull();
    }

    @Test
    @DisplayName("Train 60000 records, 5 epochs")
    @Disabled("Long running test")
    @Tag("slow")
    public void train60000and5epochs() {
        //NetworkTrainer.setPrintStream(System.out);
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()), 5);
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.95);
    }

    @Test
    @DisplayName("Train, validate and save wrong to file")
    @Disabled("Long running test")
    @Tag("slow")
    public void trainValidateAnsSaveWrongRecords() throws IOException {
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()), 1);
        assertThat(duration).isNotNull();

        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();

        ValidationReport validationReport = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile()));
        assertThat(validationReport.getPerformance()).isGreaterThan(0.8);

        List<String> incorrectRecords = validationReport.getIncorrectRecords();
        if (!incorrectRecords.isEmpty()) {
            File file = new File("wrong.csv");
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }

            Files.write(file.toPath(), incorrectRecords, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}
