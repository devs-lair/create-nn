package devs.lair.nn;

import java.util.function.DoubleFunction;

public class MatrixUtils {

    private MatrixUtils() {
        throw new UnsupportedOperationException();
    }

    public static double[][] multiply(double[][] ma, double[][] mb) {
        checkCompatible(ma, mb);

        double[][] result = new double[ma.length][mb[0].length];

        for (int row = 0; row < result.length; row++) {
            for (int col = 0; col < result[row].length; col++) {
                result[row][col] = calculateElement(ma, mb, row, col);
            }
        }

        return result;
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

    public static void checkCompatible(double[][] ma, double[][] mb) {
        checkEmpty(ma);
        checkEmpty(mb);

        checkConstantLength(ma);
        checkConstantLength(mb);

        if (ma[0].length != mb.length) {
            throw new IllegalArgumentException("Matrices not compatible");
        }
    }

    public static void checkEmpty(double[][] m) {
        if (m.length == 0) {
            throw new IllegalArgumentException("Matrix is empty");
        }
    }

    public static void checkEmpty(double[] m) {
        if (m.length == 0) {
            throw new IllegalArgumentException("Matrix is empty");
        }
    }

    public static void checkConstantLength(double[][] m) {
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
}
