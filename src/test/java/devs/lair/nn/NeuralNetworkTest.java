package devs.lair.nn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NeuralNetworkTest {

    @Test
    @DisplayName("Create network and query with ones in weights")
    void createNetworkWithOnes() {
        NeuralNetwork nn = new NeuralNetwork(3, 3,3, 0.3);
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
        NeuralNetwork nn = new NeuralNetwork(3, 3,3, 0.3);
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
        NeuralNetwork nn = new NeuralNetwork(3, 3,3, 0.3);
            double[][] answer = nn.query(new double[] {1.0, 1.0, 1.0});
        assertThat(answer).hasDimensions(3, 1);
    }

    //========== Negative Test ====== //

    @Test
    @DisplayName("Wrong parameters in constructor")
    void wrongParametersInConstructor() {
        assertThatThrownBy(() -> new NeuralNetwork(0, 1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new NeuralNetwork(1, 0, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new NeuralNetwork(1, 1, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    @DisplayName("Wrong inputs size in query")
    void wrongInputsSizeInQuery() {
        NeuralNetwork nn = new NeuralNetwork(3, 3,3, 0.3);
        assertThatThrownBy(() -> nn.query(new double[] {1}))
                .isInstanceOf(IllegalArgumentException.class);

    }
}
