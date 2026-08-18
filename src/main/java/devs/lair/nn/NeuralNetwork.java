package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleFunction;

public class NeuralNetwork {

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;
    private final DoubleFunction<Double> activationFunction
            = (double x) -> 1 / (1 + Math.exp(-x));
    private final DoubleFunction<Double> inverseActivationFunction
            = (double y) -> Math.log(y / (1 - y));

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

    public void train(double[][] inputMatrix, double[][] targetMatrix) {
//        if (inputs.length != inputNodesNumber) {
//            throw new IllegalArgumentException("Wrong count of inputs");
//        }
//
//        if (targets.length != outputNodesNumber) {
//            throw new IllegalArgumentException("Wrong count of outputs");
//        }

//        double[][] inputMatrix = MatrixUtils.transformToMatrix(inputs);
//        double[][] targetMatrix = MatrixUtils.transformToMatrix(targets);
        //long start = System.currentTimeMillis();
        double[][] hiddenOutputs = MatrixUtils.multiplyAndApplyFunction(inputToHiddenWeights, inputMatrix, activationFunction);
        double[][] finalOutputs = MatrixUtils.multiplyAndApplyFunction(hiddenToOutputsWeights, hiddenOutputs, activationFunction);

        double[][] outputErrors = MatrixUtils.subtract(targetMatrix, finalOutputs);
        double[][] hiddenErrors = MatrixUtils.transposeFirstAndMultiply(hiddenToOutputsWeights, outputErrors);

        //System.out.println("1 Before delta " + (System.currentTimeMillis() - start) );
        //self.who += self.lr * numpy.dot((output_errors * final_outputs * (1.0 - final_outputs)), numpy.transpose(hidden_outputs))
        double[][] deltaHiddenToOutputs =
                MatrixUtils.multiplyOnTransposeAndApply(
                        MatrixUtils.multiplyByElements(List.of(outputErrors, finalOutputs, finalOutputs), Arrays.asList(null, null, (d) -> 1 - d)),
                        hiddenOutputs, (d) -> d * learningRate);

        hiddenToOutputsWeights = MatrixUtils.add(hiddenToOutputsWeights, deltaHiddenToOutputs);

        //System.out.println("2 Before delta " + (System.currentTimeMillis() - start) );
        //self.wih += self.lr * numpy.dot((hidden_errors * hidden_outputs * (1.0 - hidden_outputs)), numpy.transpose(inputs))
        double[][] deltaInputsToHidden = MatrixUtils.multiplyOnTransposeAndApply(
                        MatrixUtils.multiplyByElements(List.of(hiddenErrors, hiddenOutputs, hiddenOutputs), Arrays.asList(null, null, (d) -> 1 - d)),
                        inputMatrix,
                        (d) -> d * learningRate);

        //System.out.println("Before add " + (System.currentTimeMillis() - start) );
        inputToHiddenWeights = MatrixUtils.add(inputToHiddenWeights, deltaInputsToHidden);
        //System.out.println("After all " + (System.currentTimeMillis() - start) );
    }

    public double[][] query(double[][] inputMatrix) {
        if (inputMatrix.length != inputNodesNumber) {
            throw new IllegalArgumentException("Wrong count of inputs");
        }

        //double[][] inputMatrix = MatrixUtils.transformToMatrix(inputs);
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
