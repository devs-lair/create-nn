package devs.lair.nn;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Stream;

public class MatrixUtils {
    private static boolean noChecks = false;

    private MatrixUtils() {
        throw new UnsupportedOperationException();
    }

    public static void setNoChecks(boolean noChecks) {
        MatrixUtils.noChecks = noChecks;
    }

    public static double[][] multiply(double[][] ma, double[][] mb) {
        checkMultiplyMatrixCompatible(ma, mb);

        double[][] result = new double[ma.length][mb[0].length];

        for (int row = 0; row < result.length; row++) {
            for (int col = 0; col < result[row].length; col++) {
                result[row][col] = calculateElement(ma, mb, row, col);
            }
        }

        return result;
    }

    public static double[][] multiplyByElements(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.MULTIPLY);
    }

    public static double[][] divideByElements(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.DIVIDE);
    }

    public static double[][] multiply(double[][] m, double scalar) {
        checkEmpty(m);
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = m[i][j] * scalar;
            }
        }
        return m;
    }

    public static double[][] transpose(double[][] m) {
        checkEmpty(m);
        checkConstantLength(m);

        int rows = m.length;
        int cols = m[0].length;

        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = m[i][j];
            }
        }

        return transposed;
    }

    public static double[][] transpose(double[] a) {
        checkEmpty(a);

        int length = a.length;
        double[][] transposed = new double[length][1];
        for (int i = 0; i < length; i++) {
            transposed[i][0] = a[i];
        }

        return transposed;
    }

    private static double calculateElement(double[][] ma, double[][] mb, int row, int col) {
        double cell = 0;
        for (int i = 0; i < mb.length; i++) {
            cell += ma[row][i] * mb[i][col];
        }
        return cell;
    }

    public static double[][] transformToMatrix(double[] inputs) {
        double[][] matrix = new double[inputs.length][1];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i][0] = inputs[i];
        }

        return matrix;
    }

    public static double[][] applyFunction(double[][] matrix, DoubleFunction<Double> doubleFunction) {
        double[][] result = new double[matrix.length][matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = doubleFunction.apply(matrix[i][j]);
            }
        }

        return result;
    }

    public static double[][] subtract(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.SUBTRACT);
    }

    public static double[][] add(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.ADD);
    }

    public static double[][] subtract(double scalar, double[][] matrix) {
        checkEmpty(matrix);

        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = scalar - matrix[i][j];
            }
        }

        return result;
    }

    public static double[][] byElements(double[][] ma, double[][] mb, @NotNull Operation operation) {
        checkElementsOperationCompatible(ma, mb);
        double[][] result = new double[ma.length][ma[0].length];
        for (int i = 0; i < ma.length; i++) {
            for (int j = 0; j < ma[0].length; j++) {
                result[i][j] = switch (operation) {
                    case ADD -> ma[i][j] + mb[i][j];
                    case SUBTRACT -> ma[i][j] - mb[i][j];
                    case MULTIPLY -> ma[i][j] * mb[i][j];
                    case DIVIDE -> ma[i][j] / mb[i][j];
                };
            }
        }

        return result;
    }

    public static void checkMultiplyMatrixCompatible(double[][] ma, double[][] mb) {
        if (noChecks) return;

        checkEmpty(ma);
        checkEmpty(mb);

        checkConstantLength(ma);
        checkConstantLength(mb);

        if (ma[0].length != mb.length) {
            throw new IllegalArgumentException("Matrices not compatible");
        }
    }

    public static void checkEmpty(double[][] m) {
        if (noChecks) return;

        if (m.length == 0) {
            throw new IllegalArgumentException("Matrix is empty");
        }
    }

    public static void checkEmpty(double[] m) {
        if (noChecks) return;

        if (m.length == 0) {
            throw new IllegalArgumentException("Matrix is empty");
        }
    }

    public static void checkConstantLength(double[][] m) {
        if (noChecks) return;

        int constantLength = -1;
        for (int i = 0; i < m.length; i++) {
            if (constantLength == - 1) {
                constantLength = m[i].length;
                continue;
            }

            if (constantLength != m[i].length ) {
                throw new IllegalArgumentException(
                        "Martix has an inconstant size, wrong row with index = %d".formatted(i));
            }
        }

        if (constantLength == 0) {
            throw new IllegalArgumentException("Matrix has empty rows");
        }
    }

    private static void checkElementsOperationCompatible(double[][] ma, double[][] mb) {
        if (noChecks) return;

        checkEmpty(ma);

        if (ma.length != mb.length) {
            throw new IllegalArgumentException("Matrices not compatible");
        }

        if (ma[0].length != mb[0].length) {
            throw new IllegalArgumentException("Matrices not compatible");
        }

        checkConstantLength(ma);
        checkConstantLength(mb);
    }

    public static String toString(double[][] matrix) {
        StringBuilder result = new StringBuilder();
        for (double[] row : matrix) {
            for (double v : row) {
                result.append(v).append(",").append("\n");
            }
        }

        result.delete(result.length() - 1, result.length());
        return result.toString();
    }

    public enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }
}
