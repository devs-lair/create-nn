package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.apache.commons.math3.util.FastMath;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleOperations;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.DoubleFunction;

public class NeuralNetworkEjml implements INeuralNetwork {

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;

    private final ApplyDoubleFunctionOp activationFunction;
    private final ScalarMinusMatrixOp scalarMinusMatrixOp;

    private final ReentrantLock weightsLock = new ReentrantLock();

    private SimpleMatrix inputToHiddenWeights;
    private SimpleMatrix hiddenToOutputsWeights;
    private WeightInitStrategy weightInitStrategy = WeightInitStrategy.RANDOM_GAUSSIAN;

    public NeuralNetworkEjml(int inputNodesNumber,
                             int hiddenNodesNumber,
                             int outputNodesNumber,
                             double learningRate) {

        Checker.checkNodesNumbers(inputNodesNumber, hiddenNodesNumber, outputNodesNumber);

        this.inputNodesNumber = inputNodesNumber;
        this.hiddenNodesNumber = hiddenNodesNumber;
        this.outputNodesNumber = outputNodesNumber;
        this.learningRate = learningRate;

        activationFunction =
                new ApplyDoubleFunctionOp((double x) -> 1 / (1 + FastMath.exp(-x)));
        scalarMinusMatrixOp = new ScalarMinusMatrixOp(1d);

        initWeights();
    }

    private void initWeights() {
        inputToHiddenWeights = initWeightsMatrix(hiddenNodesNumber, inputNodesNumber);
        hiddenToOutputsWeights = initWeightsMatrix(outputNodesNumber, hiddenNodesNumber);
    }

    private SimpleMatrix initWeightsMatrix(int rows, int columns) {
        return switch (weightInitStrategy) {
            case ONES -> SimpleMatrix.ones(rows, columns);
            case ZEROS -> new SimpleMatrix(rows, columns);
            case RANDOM_GAUSSIAN -> {
                Random rnd = new Random(); // Инициализация генератора случайных чисел
                DMatrixRMaj dMatrixRMaj = RandomMatrices_DDRM.rectangleGaussian(rows, columns,
                        0, FastMath.pow(rows, -0.5), rnd);
                yield SimpleMatrix.wrap(dMatrixRMaj);
            }
        };
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

            SimpleMatrix inputMatrix = new SimpleMatrix(MatrixUtils.transformToMatrix(inputs));
            SimpleMatrix targetMatrix = new SimpleMatrix(MatrixUtils.transformToMatrix(targets));

            SimpleMatrix currentInputToHiddenWeights;
            SimpleMatrix currentHiddenToOutputsWeights;

            try {
                weightsLock.lock();
                currentInputToHiddenWeights = inputToHiddenWeights;
                currentHiddenToOutputsWeights = hiddenToOutputsWeights;
            } finally {
                weightsLock.unlock();
            }

            SimpleMatrix hiddenInputs = currentInputToHiddenWeights.mult(inputMatrix);
            SimpleMatrix hiddenOutputs = hiddenInputs.elementOp(activationFunction);
            SimpleMatrix finalInputs = currentHiddenToOutputsWeights.mult(hiddenOutputs);
            SimpleMatrix finalOutputs = finalInputs.elementOp(activationFunction);

            SimpleMatrix outputErrors = targetMatrix.minus(finalOutputs);
            SimpleMatrix hiddenErrors = currentHiddenToOutputsWeights.transpose().mult(outputErrors);

            SimpleMatrix deltaHiddenToOutputs = finalOutputs.elementOp(scalarMinusMatrixOp).elementMult(finalOutputs)
                    .elementMult(outputErrors).mult(hiddenOutputs.transpose()).scale(learningRate);

            SimpleMatrix deltaInputsToHidden = hiddenOutputs.elementOp(scalarMinusMatrixOp).elementMult(hiddenOutputs)
                    .elementMult(hiddenErrors).mult(inputMatrix.transpose()).scale(learningRate);

            adjustWeights(
                    currentInputToHiddenWeights,
                    currentHiddenToOutputsWeights,
                    deltaInputsToHidden,
                    deltaHiddenToOutputs);
        }
    }

    private void adjustWeights(SimpleMatrix currentInputToHiddenWeights,
                               SimpleMatrix currentHiddenToOutputsWeights,
                               SimpleMatrix deltaInputsToHidden,
                               SimpleMatrix deltaHiddenToOutputs) {
        try {
            weightsLock.lock();
            inputToHiddenWeights = currentInputToHiddenWeights.plus(deltaInputsToHidden);
            hiddenToOutputsWeights = currentHiddenToOutputsWeights.plus(deltaHiddenToOutputs);
        } finally {
            weightsLock.unlock();
        }
    }

    public void train(double[] inputs, double[] targets) {
        train(List.of(new TrainRecord(inputs, targets)));
    }

    @Override
    public double[][] query(double[] inputs) {
        if (inputs.length != inputNodesNumber) {
            throw new IllegalArgumentException("Wrong count of inputs");
        }

        SimpleMatrix inputMatrix = new SimpleMatrix(MatrixUtils.transformToMatrix(inputs));
        SimpleMatrix hiddenInputs = inputToHiddenWeights.mult(inputMatrix);
        SimpleMatrix hiddenOutputs = hiddenInputs.elementOp(activationFunction);
        SimpleMatrix finalInputs = hiddenToOutputsWeights.mult(hiddenOutputs);

        return finalInputs.elementOp(activationFunction).toArray2();
    }

    public void setWeightInitStrategy(WeightInitStrategy weightInitStrategy) {
        this.weightInitStrategy = weightInitStrategy;

        initWeights();
    }

    private record ApplyDoubleFunctionOp(@NotNull DoubleFunction<Double> doubleFunction)
            implements SimpleOperations.ElementOpReal {

        @Override
        public double op(int i, int i1, double v) {
            return doubleFunction.apply(v);
        }
    }

    private record ScalarMinusMatrixOp(@NotNull Double scalar)
            implements SimpleOperations.ElementOpReal {

        @Override
        public double op(int i, int i1, double v) {
            return scalar - v;
        }
    }
}
