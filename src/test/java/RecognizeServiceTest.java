import devs.lair.nn.*;
import devs.lair.nn.ui.MnistCsvViewer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class RecognizeServiceTest {

    @Test
    @DisplayName("Try to recognize")
    void recognizeServiceTest() {
        INeuralNetwork recognizeService = new RecognizeService();

        //validate
        URL validateFile = MnistCsvViewer.class.getResource("/mnist/mnist_test.csv");
        assertThat(validateFile).isNotNull();
        double performance = NetworkTrainer.validateNetwork(recognizeService,
                Paths.get(validateFile.getFile())).getPerformance();

        assertThat(performance).isGreaterThan(0.9);
        System.out.println(performance);
    }
 }
