package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.apache.commons.math3.util.FastMath;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DOperatorUnary;
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

    private final DOperatorUnary activationFunction = (double v) -> 1 / (1 + Math.exp(-v));

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
                Random rnd = new Random(); // Инициализация генератора случайных чисел
                yield RandomMatrices_DDRM.rectangleGaussian(rows, columns,
                        0, FastMath.pow(rows, -0.5), rnd);
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

            DMatrixRMaj inputMatrix = new DMatrixRMaj(MatrixUtils.transformToMatrix(inputs));
            DMatrixRMaj targetMatrix = new DMatrixRMaj(MatrixUtils.transformToMatrix(targets));

            DMatrixRMaj currentInputToHiddenWeights;
            DMatrixRMaj currentHiddenToOutputsWeights;

            try {
                weightsLock.lock();
                currentInputToHiddenWeights = inputToHiddenWeights;
                currentHiddenToOutputsWeights = hiddenToOutputsWeights;
            } finally {
                weightsLock.unlock();
            }

//            DMatrixRMaj hiddenInputs = new DMatrixRMaj();
//            DMatrixRMaj hiddenOutputs = new DMatrixRMaj();
//            DMatrixRMaj finalInputs = new DMatrixRMaj();
//            DMatrixRMaj finalOutputs = new DMatrixRMaj();
//            DMatrixRMaj outputErrors = new DMatrixRMaj();
//            DMatrixRMaj hiddenErrors = new DMatrixRMaj();

            DMatrixRMaj hiddenInputs = CommonOps_DDRM.mult(currentInputToHiddenWeights, inputMatrix, null);
            DMatrixRMaj hiddenOutputs = CommonOps_DDRM.apply(hiddenInputs, activationFunction, null);
            DMatrixRMaj finalInputs = CommonOps_DDRM.mult(currentHiddenToOutputsWeights, hiddenOutputs, null);
            DMatrixRMaj finalOutputs = CommonOps_DDRM.apply(finalInputs, activationFunction);

            DMatrixRMaj outputErrors = CommonOps_DDRM.subtract(targetMatrix, finalOutputs, null);
            DMatrixRMaj hiddenErrors = CommonOps_DDRM.multTransA(currentHiddenToOutputsWeights, outputErrors, null);

            DMatrixRMaj deltaHiddenToOutputs = CommonOps_DDRM.multTransB(learningRate,
                    CommonOps_DDRM.elementMult(outputErrors,
                            CommonOps_DDRM.elementMult(finalOutputs,
                                    CommonOps_DDRM.subtract(1, finalOutputs, null), null), null),
                    hiddenOutputs, null);


            DMatrixRMaj deltaInputsToHidden = CommonOps_DDRM.multTransB(learningRate,
                    CommonOps_DDRM.elementMult(hiddenErrors,
                            CommonOps_DDRM.elementMult(hiddenOutputs,
                                    CommonOps_DDRM.subtract(1, hiddenOutputs, null), null), null),
                    inputMatrix, null);

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

        //SimpleMatrix inputMatrix = new SimpleMatrix(MatrixUtils.transformToMatrix(inputs));
        DMatrixRMaj inputMatrix = new DMatrixRMaj(MatrixUtils.transformToMatrix(inputs));

        //SimpleMatrix hiddenInputs = inputToHiddenWeights.mult(inputMatrix);
        DMatrixRMaj hiddenInputs = new DMatrixRMaj();
        CommonOps_DDRM.mult(inputToHiddenWeights, inputMatrix, hiddenInputs);

        //SimpleMatrix hiddenOutputs = hiddenInputs.elementOp(activationFunctionOp);
        DMatrixRMaj hiddenOutputs = new DMatrixRMaj();
        CommonOps_DDRM.apply(hiddenInputs, activationFunction, hiddenOutputs);

        //SimpleMatrix finalInputs = hiddenToOutputsWeights.mult(hiddenOutputs);
        DMatrixRMaj finalInputs = new DMatrixRMaj();
        CommonOps_DDRM.mult(hiddenToOutputsWeights, hiddenOutputs, finalInputs);

        return CommonOps_DDRM.apply(finalInputs, activationFunction).get2DData();
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
