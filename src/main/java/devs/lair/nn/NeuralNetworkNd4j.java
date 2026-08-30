package devs.lair.nn;

import devs.lair.nn.util.Checker;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.distribution.impl.NormalDistribution;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class NeuralNetworkNd4j implements INeuralNetwork {

    private final int inputNodesNumber;
    private final int hiddenNodesNumber;
    private final int outputNodesNumber;
    private final double learningRate;

    private final ReentrantLock weightsLock = new ReentrantLock();
    private INDArray inputToHiddenWeights;
    private INDArray hiddenToOutputsWeights;

    private WeightInitStrategy weightInitStrategy = WeightInitStrategy.RANDOM_GAUSSIAN;

    public NeuralNetworkNd4j(int inputNodesNumber,
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

    private INDArray initWeightsMatrix(int rows, int columns) {
        return switch (weightInitStrategy) {
            case ONES -> Nd4j.ones(DataType.DOUBLE, rows, columns);
            case ZEROS -> Nd4j.zeros(DataType.DOUBLE, rows, columns);
            case RANDOM_GAUSSIAN -> Nd4j.rand(new NormalDistribution(0,
                    Math.pow(rows, -0.5)), rows, columns).castTo(DataType.DOUBLE);
        };
    }

    @Override
    public void train(@NotNull List<TrainRecord> batch) {
        INDArray inputMatrix = null;
        INDArray targetMatrix = null;
        INDArray hiddenInputs = null;
        INDArray hiddenOutputs = null;
        INDArray finalOutputs = null;
        INDArray outputErrors = null;
        INDArray hiddenErrors = null;
        INDArray currentHiddenToOutputsWeights = null;
        INDArray currentInputToHiddenWeights = null;

        for (TrainRecord trainRecord : batch) {
            double[] inputs = trainRecord.inputs();
            double[] targets = trainRecord.targets();

            if (inputs.length != inputNodesNumber) {
                throw new IllegalArgumentException("Wrong count of inputs");
            }

            if (targets.length != outputNodesNumber) {
                throw new IllegalArgumentException("Wrong count of outputs");
            }

            inputMatrix = Nd4j.create(MatrixUtils.transformToMatrix(inputs));
            targetMatrix = Nd4j.create(MatrixUtils.transformToMatrix(targets));

            try {
                weightsLock.lock();
                currentInputToHiddenWeights = inputToHiddenWeights;
                currentHiddenToOutputsWeights = hiddenToOutputsWeights;
            } finally {
                weightsLock.unlock();
            }

            hiddenInputs = currentInputToHiddenWeights.mmul(inputMatrix);
            hiddenOutputs = Transforms.sigmoid(hiddenInputs);
            finalOutputs = Transforms.sigmoid(currentHiddenToOutputsWeights.mmul(hiddenOutputs));
            outputErrors = targetMatrix.sub(finalOutputs);
            hiddenErrors = currentHiddenToOutputsWeights.transpose().mmul(outputErrors);

            INDArray deltaHiddenToOutputs = finalOutputs.rsub(1).mul(finalOutputs)
                    .mul(outputErrors).mmul(hiddenOutputs.transpose()).mul(learningRate);

            INDArray deltaInputsToHidden = hiddenOutputs.rsub(1).mul(hiddenOutputs)
                    .mul(hiddenErrors).mmul(inputMatrix.transpose()).mul(learningRate);

            adjustWeights(deltaInputsToHidden, deltaHiddenToOutputs);
        }

        inputMatrix.close();
        targetMatrix.close();
        hiddenInputs.close();
        hiddenOutputs.close();
        finalOutputs.close();
        outputErrors.close();
        hiddenErrors.close();
        currentHiddenToOutputsWeights.close();
        currentInputToHiddenWeights.close();
    }

    private void adjustWeights(INDArray deltaInputsToHidden, INDArray deltaHiddenToOutputs) {
        try {
            weightsLock.lock();
            inputToHiddenWeights = inputToHiddenWeights.add(deltaInputsToHidden);
            hiddenToOutputsWeights = hiddenToOutputsWeights.add(deltaHiddenToOutputs);

            deltaInputsToHidden.close();
            deltaHiddenToOutputs.close();

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

        INDArray inputMatrix = Nd4j.create(MatrixUtils.transformToMatrix(inputs));
        INDArray hiddenInputs = inputToHiddenWeights.mmul(inputMatrix);
        INDArray hiddenOutputs = Transforms.sigmoid(hiddenInputs);
        INDArray finalInputs = hiddenToOutputsWeights.mmul(hiddenOutputs);
        double[][] result = Transforms.sigmoid(finalInputs).toDoubleMatrix();

        inputMatrix.close();;
        hiddenInputs.close();
        hiddenOutputs.close();
        finalInputs.close();

        return result;
    }

    public void setWeightInitStrategy(WeightInitStrategy weightInitStrategy) {
        this.weightInitStrategy = weightInitStrategy;

        initWeights();
    }
}
