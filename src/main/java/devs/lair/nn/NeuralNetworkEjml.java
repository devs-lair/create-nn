package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.apache.commons.math3.util.FastMath;
import org.ejml.data.DMatrixD1;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DOperatorUnary;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class NeuralNetworkEjml implements INeuralNetwork {

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;

    private final DOperatorUnary activationFunction = (double v) -> 1 / (1 + FastMath.exp(-v));
    private final ReentrantLock weightsLock = new ReentrantLock();

    private DMatrixRMaj inputToHiddenWeights;
    private DMatrixRMaj hiddenToOutputsWeights;
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

        initWeights();
    }

    private void initWeights() {
        inputToHiddenWeights = initWeightsMatrix(hiddenNodesNumber, inputNodesNumber);
        hiddenToOutputsWeights = initWeightsMatrix(outputNodesNumber, hiddenNodesNumber);
    }

    private DMatrixRMaj initWeightsMatrix(int rows, int columns) {
        return switch (weightInitStrategy) {
            case ONES -> SimpleMatrix.ones(rows, columns).getMatrix();
            case ZEROS -> new SimpleMatrix(rows, columns).getMatrix();
            case RANDOM_GAUSSIAN -> {
                Random rnd = new Random();
                yield RandomMatrices_DDRM.rectangleGaussian(rows, columns,
                        0, FastMath.pow(rows, -0.5), rnd);
            }
        };
    }

    @Override
    public void train(@NotNull List<TrainRecord> batch) {
        DMatrixRMaj hiddenOutputs = new DMatrixRMaj();
        DMatrixRMaj finalOutputs = new DMatrixRMaj();
        DMatrixRMaj outputErrors = new DMatrixRMaj();
        DMatrixRMaj hiddenErrors = new DMatrixRMaj();
        DMatrixRMaj deltaHiddenToOutputs = new DMatrixRMaj();
        DMatrixRMaj deltaInputsToHidden = new DMatrixRMaj();
        DMatrixRMaj inputMatrix = new DMatrixRMaj();
        DMatrixRMaj targetMatrix = new DMatrixRMaj();
        DMatrixRMaj currentInputToHiddenWeights;
        DMatrixRMaj currentHiddenToOutputsWeights;

        for (TrainRecord trainRecord : batch) {
            double[] inputs = trainRecord.inputs();
            double[] targets = trainRecord.targets();

            if (inputs.length != inputNodesNumber) {
                throw new IllegalArgumentException("Wrong count of inputs");
            }

            if (targets.length != outputNodesNumber) {
                throw new IllegalArgumentException("Wrong count of outputs");
            }

            inputMatrix.set(inputs.length, 1, true, inputs);
            targetMatrix.set(targets.length, 1, true, targets);

            try {
                weightsLock.lock();
                currentInputToHiddenWeights = inputToHiddenWeights;
                currentHiddenToOutputsWeights = hiddenToOutputsWeights;
            } finally {
                weightsLock.unlock();
            }

            CommonOps_DDRM.apply(CommonOps_DDRM.mult(currentInputToHiddenWeights, inputMatrix, null),
                    activationFunction, hiddenOutputs);

            CommonOps_DDRM.apply(CommonOps_DDRM.mult(currentHiddenToOutputsWeights, hiddenOutputs, null),
                    activationFunction, finalOutputs);

            CommonOps_DDRM.subtract(targetMatrix, finalOutputs, outputErrors);
            CommonOps_DDRM.multTransA(currentHiddenToOutputsWeights, outputErrors, hiddenErrors);

            CommonOps_DDRM.multTransB(learningRate,
                    CommonOps_DDRM.elementMult(outputErrors,
                            CommonOps_DDRM.elementMult(finalOutputs,
                                    CommonOps_DDRM.subtract(1, finalOutputs, null), null), null),
                    hiddenOutputs, deltaHiddenToOutputs);

            CommonOps_DDRM.multTransB(learningRate,
                    CommonOps_DDRM.elementMult(hiddenErrors,
                            CommonOps_DDRM.elementMult(hiddenOutputs,
                                    CommonOps_DDRM.subtract(1, hiddenOutputs, null), null), null),
                    inputMatrix, deltaInputsToHidden);

            adjustWeights(
                    currentInputToHiddenWeights,
                    currentHiddenToOutputsWeights,
                    deltaInputsToHidden,
                    deltaHiddenToOutputs);
        }
    }

    private void adjustWeights(DMatrixRMaj currentInputToHiddenWeights,
                               DMatrixRMaj currentHiddenToOutputsWeights,
                               DMatrixRMaj deltaInputsToHidden,
                               DMatrixRMaj deltaHiddenToOutputs) {
        try {
            weightsLock.lock();
            inputToHiddenWeights = CommonOps_DDRM.add(currentInputToHiddenWeights, deltaInputsToHidden, null);
            hiddenToOutputsWeights = CommonOps_DDRM.add(currentHiddenToOutputsWeights, deltaHiddenToOutputs, null);
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

        DMatrixRMaj inputMatrix = new DMatrixRMaj(MatrixUtils.transformToMatrix(inputs));
        DMatrixRMaj hiddenInputs = CommonOps_DDRM.mult(inputToHiddenWeights, inputMatrix, null);
        DMatrixRMaj hiddenOutputs = CommonOps_DDRM.apply(hiddenInputs, activationFunction, null);
        DMatrixRMaj finalInputs = CommonOps_DDRM.mult(hiddenToOutputsWeights, hiddenOutputs, null);

        return CommonOps_DDRM.apply(finalInputs, activationFunction).get2DData();
    }

    public void setWeightInitStrategy(WeightInitStrategy weightInitStrategy) {
        this.weightInitStrategy = weightInitStrategy;
        initWeights();
    }

    private <T extends DMatrixD1> T elementMult(T... m) {
        T A = m[0];

        DMatrixRMaj output = new DMatrixRMaj(A.numRows, A.numCols);
        int length = A.getNumElements();


        for (int i = 0; i < length; i++) {
            double cell = 1;
            for (T matrix : m) {
                cell *= matrix.get(i);
            }

            output.set(i, cell);
        }

        return (T) output;
    }
}
