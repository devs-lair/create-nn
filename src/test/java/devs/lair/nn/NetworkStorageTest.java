package devs.lair.nn;

import devs.lair.nn.ui.MnistCsvViewer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NetworkStorageTest {

    @BeforeEach
    void setUp() {
        MatrixUtils.setNoChecks(true);
        NetworkTrainer.setPrintStream(null);
    }

    @AfterEach
    void afterEach() {
        MatrixUtils.setNoChecks(false);
        NetworkTrainer.setPrintStream(System.out);
    }

    @Test
    @DisplayName("Save nn to file")
    @Order(1)
    void saveToFile() {
        NetworkTrainer.setPrintStream(System.out);
        NeuralNetwork nn = new NeuralNetwork(784, 200, 10, 0.1);

        //train
        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(trainFile).isNotNull();
        NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()), 5);

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();

        assertThat(performance).isGreaterThan(0.95);

        File file = NetworkStorage.saveToFile(nn, Paths.get("nn-test.csv"));
        assertThat(file).isNotEmpty();
    }

    @Test
    @DisplayName("Load nn from file")
    @Order(2)
    void loadFromFile() {
        //NetworkTrainer.setPrintStream(System.out);
        NeuralNetwork nn = NetworkStorage.loadFromFile(Paths.get("nn-test.csv"));
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        assertThat(validateFile).isNotNull();

        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.5);
    }
}
