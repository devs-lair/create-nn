package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.jetbrains.annotations.NotNull;
import java.util.Random;
import java.util.function.DoubleFunction;

public class NeuralNetwork {

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;
    private final DoubleFunction<Double> activationFunction
            = (double x) -> 1 / (1 + Math.exp(-x));

    private double[][] inputToHiddenWeights;
    private double[][] hiddenToOutputsWeights;
    private WeightInitStrategy weightInitStrategy = WeightInitStrategy.RANDOM_GAUSSIAN;

    public NeuralNetwork(int inputNodesNumber,
                         int hiddenNodesNumber,
                         int outputNodesNumber,
                         double learningRate) {

        Checker.checkNodesNumbers(inputNodesNumber, hiddenNodesNumber, outputNodesNumber);

        this.inputNodesNumber = inputNodesNumber;
        this.hiddenNodesNumber = hiddenNodesNumber;
        this.outputNodesNumber = outputNodesNumber;
        this.learningRate = learningRate;

        initWeights();
    }

    public void train(double[] inputs, double[] targets) {
        if (inputs.length != inputNodesNumber) {
            throw new IllegalArgumentException("Wrong count of inputs");
        }

        if (targets.length != outputNodesNumber) {
            throw new IllegalArgumentException("Wrong count of outputs");
        }

        double[][] inputMatrix = MatrixUtils.transformToMatrix(inputs);
        double[][] targetMatrix = MatrixUtils.transformToMatrix(targets);

        double[][] hiddenInputs = MatrixUtils.multiply(inputToHiddenWeights, inputMatrix);
        double[][] hiddenOutputs = MatrixUtils.applyFunction(hiddenInputs, activationFunction);
        double[][] finalInputs = MatrixUtils.multiply(hiddenToOutputsWeights, hiddenOutputs);
        double[][] finalOutputs = MatrixUtils.applyFunction(finalInputs, activationFunction);

        double[][] outputErrors = MatrixUtils.subtract(targetMatrix, finalOutputs);
        double[][] hiddenErrors = MatrixUtils.multiply(MatrixUtils.transpose(hiddenToOutputsWeights), outputErrors);

        //self.who += self.lr * numpy.dot((output_errors * final_outputs * (1.0 - final_outputs)), numpy.transpose(hidden_outputs))
        double[][] deltaHiddenToOutputs = MatrixUtils.multiply(
                MatrixUtils.multiply(
                        MatrixUtils.multiplyByElements(
                                outputErrors,
                                MatrixUtils.multiplyByElements(
                                        finalOutputs,
                                        MatrixUtils.subtract(1, finalOutputs))),
                        MatrixUtils.transpose(hiddenOutputs)),
                learningRate);

        hiddenToOutputsWeights = MatrixUtils.add(hiddenToOutputsWeights, deltaHiddenToOutputs);

        //self.wih += self.lr * numpy.dot((hidden_errors * hidden_outputs * (1.0 - hidden_outputs)), numpy.transpose(inputs))
        double[][] deltaInputsToHidden = MatrixUtils.multiply(
                MatrixUtils.multiply(
                        MatrixUtils.multiplyByElements(
                                hiddenErrors,
                                MatrixUtils.multiplyByElements(
                                        hiddenOutputs,
                                        MatrixUtils.subtract(1, hiddenOutputs))),
                        MatrixUtils.transpose(inputMatrix)),
                learningRate);

        inputToHiddenWeights = MatrixUtils.add(inputToHiddenWeights, deltaInputsToHidden);
    }

    public double[][] query(double[] inputs) {
        if (inputs.length != inputNodesNumber) {
            throw new IllegalArgumentException("Wrong count of inputs");
        }

        double[][] inputMatrix = MatrixUtils.transformToMatrix(inputs);
        double[][] hiddenInputs = MatrixUtils.multiply(inputToHiddenWeights, inputMatrix);
        double[][] hiddenOutputs = MatrixUtils.applyFunction(hiddenInputs, activationFunction);
        double[][] finalInputs = MatrixUtils.multiply(hiddenToOutputsWeights, hiddenOutputs);

        return MatrixUtils.applyFunction(finalInputs, activationFunction);
    }

    public void setWeightInitStrategy(@NotNull WeightInitStrategy weightInitStrategy) {
        this.weightInitStrategy = weightInitStrategy;
        initWeights();
    }

    private void initWeights() {
        inputToHiddenWeights = new double[hiddenNodesNumber][inputNodesNumber];
        hiddenToOutputsWeights = new double[outputNodesNumber][hiddenNodesNumber];

        fillWeightMatrix(inputToHiddenWeights);
        fillWeightMatrix(hiddenToOutputsWeights);
    }

    public void initWeights(double[][] ihw, double[][] how) {
        inputToHiddenWeights = ihw;
        hiddenToOutputsWeights = how;
    }

    private void fillWeightMatrix(double[][] matrix) {
        Random random = new Random();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = switch (weightInitStrategy) {
                    case ONES -> 1;
                    case ZEROS -> 0;
                    case RANDOM_GAUSSIAN -> random.nextGaussian(0, Math.pow(matrix.length, -0.5));
                };
            }
        }
    }

    //==== Getters ==== //

    public int getInputNodesNumber() {
        return inputNodesNumber;
    }

    public int getHiddenNodesNumber() {
        return hiddenNodesNumber;
    }

    public int getOutputNodesNumber() {
        return outputNodesNumber;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double[][] getInputToHiddenWeights() {
        return inputToHiddenWeights;
    }

    public double[][] getHiddenToOutputsWeights() {
        return hiddenToOutputsWeights;
    }
}
