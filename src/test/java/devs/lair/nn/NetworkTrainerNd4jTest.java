package devs.lair.nn;

import devs.lair.nn.ui.MnistCsvViewer;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkTrainerNd4jTest {

    @BeforeEach
    void beforeEach() {
        MatrixUtils.setNoChecks(true);
        NetworkTrainer.setPrintStream(null);
    }

    @AfterEach
    void afterEach() {
        MatrixUtils.setNoChecks(false);
        NetworkTrainer.setPrintStream(System.out);
    }

    @Test
    @DisplayName("Train 10000 records on nd4j network")
    @Disabled("Long running test")
    @Tag("slow")
    public void train10000recordsNd4j() {
        NeuralNetworkNd4j nn = new NeuralNetworkNd4j(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()));
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.8);
        System.out.println(performance);
    }

    @RepeatedTest(1)
    @DisplayName("Train 60000 records on nd4j network")
    @Disabled("Long running test")
    @Tag("slow")
    public void train60000recordsNd4j() {
        NeuralNetworkNd4j nn = new NeuralNetworkNd4j(784, 200, 10, 0.1);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetwork(nn, Paths.get(trainFile.getFile()));
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.8);
        System.out.println(performance);
    }


    @RepeatedTest(2)
    @DisplayName("Train 60000 records async on nd4j network")
    @Disabled("Long running test")
    @Tag("slow, not stable")
    public void train60000AsyncND4J() {
        NeuralNetworkNd4j nn = new NeuralNetworkNd4j(784, 200, 10, 0.1);
        NetworkTrainer.setPrintStream(System.out);
        MatrixUtils.setNoChecks(true);

        URL trainFile = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(trainFile).isNotNull();

        Duration duration = NetworkTrainer.trainNetworkAsync(nn, Paths.get(trainFile.getFile()), 1, 1000);
        assertThat(duration).isNotNull();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(nn, Paths.get(validateFile.getFile())).getPerformance();
        assertThat(performance).isGreaterThan(0.9);
        System.out.println(performance);
    }
}