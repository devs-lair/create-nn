package devs.lair.nn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class NeuralNetworkEjmlTest {

    @Test
    @DisplayName("Create network and query with ones in weights")
    void createNetworkWithOnes() {
        NeuralNetworkEjml nn = new NeuralNetworkEjml(3, 3,3, 0.3);
        nn.setWeightInitStrategy(WeightInitStrategy.ONES);

        double[][] answer = nn.query(new double[]{1.0, 1.0, 1.0});

        assertThat(answer).hasDimensions(3, 1);
        assertThat(answer[0][0]).isCloseTo(0.94571649, within(0.0001));
        assertThat(answer[1][0]).isCloseTo(0.94571649, within(0.0001));
        assertThat(answer[2][0]).isCloseTo(0.94571649, within(0.0001));
    }

    @Test
    @DisplayName("Create network and query with zeros in weights")
    void createNetworkWithZeros() {
        NeuralNetworkEjml nn = new NeuralNetworkEjml(3, 3,3, 0.3);
        nn.setWeightInitStrategy(WeightInitStrategy.ZEROS);

        double[][] answer = nn.query(new double[] {1.0, 1.0, 1.0});

        assertThat(answer).hasDimensions(3, 1);
        assertThat(answer[0][0]).isCloseTo(0.5, within(0.0001));
        assertThat(answer[1][0]).isCloseTo(0.5, within(0.0001));
        assertThat(answer[2][0]).isCloseTo(0.5, within(0.0001));
    }

    @Test
    @DisplayName("Create network and query with random weights")
    void createNetworkWithRandomWeights() {
        NeuralNetworkEjml nn = new NeuralNetworkEjml(3, 3,3, 0.3);
        double[][] answer = nn.query(new double[] {1.0, 1.0, 1.0});
        assertThat(answer).hasDimensions(3, 1);
    }
}
