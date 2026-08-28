package devs.lair.nn;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface INeuralNetwork {
    void train(@NotNull List<TrainRecord> batch);
    void train(double[] inputs, double[] targets);
    double[][] query(double[] inputs);
}
