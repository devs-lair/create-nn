package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.DoubleFunction;

public class NeuralNetwork implements INeuralNetwork {

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;
    private final DoubleFunction<Double> activationFunction
            = (double x) -> 1 / (1 + FastMath.exp(-x));
    private final DoubleFunction<Double> inverseActivationFunction
            = (double y) -> FastMath.log(y / (1 - y));

    private final ReentrantLock weightsLock = new ReentrantLock();
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
        train(List.of(new TrainRecord(inputs, targets)));
    }

    @Override
    public void train(@NotNull List<TrainRecord> batch) {
        for (TrainRecord trainRecord : batch) {
            double[] inputs = trainRecord.inputs();
            double[] targets = trainRecord.targets();

            if (inputs.length != inputNodesNumber) {
                throw new IllegalArgumentException("Wrong count of inputs");
            }

            if (targets.length != outputNodesNumber) {
                throw new IllegalArgumentException("Wrong count of outputs");
            }

            double[][] inputMatrix = MatrixUtils.transformToMatrix(inputs);
            double[][] targetMatrix = MatrixUtils.transformToMatrix(targets);

            double[][] currentInputToHiddenWeights;
            double[][] currentHiddenToOutputsWeights;

            try {
                weightsLock.lock();
                currentInputToHiddenWeights = inputToHiddenWeights;
                currentHiddenToOutputsWeights = hiddenToOutputsWeights;
            } finally {
                weightsLock.unlock();
            }

            double[][] hiddenInputs = MatrixUtils.multiply(currentInputToHiddenWeights, inputMatrix);
            double[][] hiddenOutputs = MatrixUtils.applyFunction(hiddenInputs, activationFunction);
            double[][] finalInputs = MatrixUtils.multiply(currentHiddenToOutputsWeights, hiddenOutputs);
            double[][] finalOutputs = MatrixUtils.applyFunction(finalInputs, activationFunction);

            double[][] outputErrors = MatrixUtils.subtract(targetMatrix, finalOutputs);
            double[][] hiddenErrors = MatrixUtils.multiply(MatrixUtils.transpose(currentHiddenToOutputsWeights), outputErrors);

            double[][] deltaHiddenToOutputs = MatrixUtils.multiply(
                    MatrixUtils.multiply(
                            MatrixUtils.multiplyByElements(
                                    outputErrors,
                                    MatrixUtils.multiplyByElements(
                                            finalOutputs,
                                            MatrixUtils.subtract(1, finalOutputs))),
                            MatrixUtils.transpose(hiddenOutputs)),
                    learningRate);

            double[][] deltaInputsToHidden = MatrixUtils.multiply(
                    MatrixUtils.multiply(
                            MatrixUtils.multiplyByElements(
                                    hiddenErrors,
                                    MatrixUtils.multiplyByElements(
                                            hiddenOutputs,
                                            MatrixUtils.subtract(1, hiddenOutputs))),
                            MatrixUtils.transpose(inputMatrix)),
                    learningRate);

            adjustWeights(deltaInputsToHidden, deltaHiddenToOutputs);
        }
    }

    private void adjustWeights(double[][] deltaInputsToHidden,
                               double[][] deltaHiddenToOutputs) {
        try {
            weightsLock.lock();
            inputToHiddenWeights = MatrixUtils.add(inputToHiddenWeights, deltaInputsToHidden);
            hiddenToOutputsWeights = MatrixUtils.add(hiddenToOutputsWeights, deltaHiddenToOutputs);
        } finally {
            weightsLock.unlock();
        }
    }

    @Override
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

    public double[][] backQuery(double[] targets) {
        double[][] finalOutputs = MatrixUtils.transformToMatrix(targets);
        double[][] finalInputs = MatrixUtils.applyFunction(finalOutputs, inverseActivationFunction);
        double[][] hiddenOutputs = MatrixUtils.multiply(MatrixUtils.transpose(hiddenToOutputsWeights), finalInputs);

        hiddenOutputs = MatrixUtils.subtract(hiddenOutputs, MatrixUtils.min(hiddenOutputs));
        hiddenOutputs = MatrixUtils.divide(hiddenOutputs, MatrixUtils.max(hiddenOutputs));
        hiddenOutputs = MatrixUtils.multiply(hiddenOutputs, 0.98);
        hiddenOutputs = MatrixUtils.add(hiddenOutputs, 0.01);

        double[][] hiddenInputs = MatrixUtils.applyFunction(hiddenOutputs, inverseActivationFunction);
        double[][] inputs = MatrixUtils.multiply(MatrixUtils.transpose(inputToHiddenWeights), hiddenInputs);

        inputs = MatrixUtils.subtract(inputs, MatrixUtils.min(inputs));
        inputs = MatrixUtils.divide(inputs, MatrixUtils.max(inputs));
        inputs = MatrixUtils.multiply(inputs, 0.98);
        inputs = MatrixUtils.add(inputs, 0.01);

        return inputs;
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
