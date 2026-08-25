package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.apache.commons.math3.util.FastMath;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DOperatorUnary;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class NeuralNetworkEjml implements INeuralNetwork {
    private final static DOperatorUnary activationFunction = (double v) -> 1 / (1 + FastMath.exp(-v));

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;

    private volatile DMatrixRMaj[] weights = new DMatrixRMaj[2];
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
        weights[0] = initWeightsMatrix(hiddenNodesNumber, inputNodesNumber);
        weights[1] = initWeightsMatrix(outputNodesNumber, hiddenNodesNumber);
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
        DMatrixRMaj[] currentWeight;

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

            currentWeight = weights;

            CommonOps_DDRM.apply(CommonOps_DDRM.mult(currentWeight[0], inputMatrix, null),
                    activationFunction, hiddenOutputs);

            CommonOps_DDRM.apply(CommonOps_DDRM.mult(currentWeight[1], hiddenOutputs, null),
                    activationFunction, finalOutputs);

            CommonOps_DDRM.subtract(targetMatrix, finalOutputs, outputErrors);
            CommonOps_DDRM.multTransA(currentWeight[1], outputErrors, hiddenErrors);

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
                    currentWeight[0],
                    currentWeight[1],
                    deltaInputsToHidden,
                    deltaHiddenToOutputs);
        }
    }

    private void adjustWeights(DMatrixRMaj currentInputToHiddenWeights,
                               DMatrixRMaj currentHiddenToOutputsWeights,
                               DMatrixRMaj deltaInputsToHidden,
                               DMatrixRMaj deltaHiddenToOutputs) {

        DMatrixRMaj[] newWeights = new DMatrixRMaj[weights.length];
        newWeights[0] = CommonOps_DDRM.add(currentInputToHiddenWeights, deltaInputsToHidden, null);
        newWeights[1] = CommonOps_DDRM.add(currentHiddenToOutputsWeights, deltaHiddenToOutputs, null);

        weights = newWeights;
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
        DMatrixRMaj hiddenInputs = CommonOps_DDRM.mult(weights[0], inputMatrix, null);
        DMatrixRMaj hiddenOutputs = CommonOps_DDRM.apply(hiddenInputs, activationFunction, null);
        DMatrixRMaj finalInputs = CommonOps_DDRM.mult(weights[1], hiddenOutputs, null);

        return CommonOps_DDRM.apply(finalInputs, activationFunction).get2DData();
    }

    public void setWeightInitStrategy(WeightInitStrategy weightInitStrategy) {
        this.weightInitStrategy = weightInitStrategy;
        initWeights();
    }
}
