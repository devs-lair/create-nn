package devs.lair.nn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.Function;

public class MatrixUtils {
    private static boolean noChecks = false;

    private MatrixUtils() {
        throw new UnsupportedOperationException();
    }

    public static void setNoChecks(boolean noChecks) {
        MatrixUtils.noChecks = noChecks;
    }

    public static double[][] transpose(double[][] matrix) {
        checkEmpty(matrix);
        checkConstantLength(matrix);

        double[][] transposed = new double[matrix[0].length][matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

    public static double[][] transpose(double[] array) {
        checkEmpty(array);

        double[][] transposed = new double[array.length][1];
        for (int i = 0; i < array.length; i++) {
            transposed[i][0] = array[i];
        }

        return transposed;
    }

    public static double[][] multiply(double[][] ma, double[][] mb) {
        checkMultiplyMatrixCompatible(ma, mb);

        double[][] result = new double[ma.length][mb[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                double cell = 0;
                for (int k = 0; k < mb.length; k++) {
                    cell += ma[i][k] * mb[k][j];
                }
                result[i][j] = cell;
            }
        }

        return result;
    }

    public static double[][] multiplyOnTransposeAndApply(double[][] ma,
                                                         double[][] mb,
                                                         @NotNull Function<Double, Double> doubleFunction) {

        double[][] result = new double[ma.length][mb.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                double cell = 0;
                for (int k = 0; k < mb[0].length; k++) {
                    cell += ma[i][k] * mb[j][k];
                }
                result[i][j] = doubleFunction.apply(cell);
            }
        }

        return result;
    }

    public static double[][] multiplyAndApplyFunction(double[][] ma,
                                                      double[][] mb,
                                                      @NotNull DoubleFunction<Double> doubleFunction) {
        checkMultiplyMatrixCompatible(ma, mb);

        double[][] result = new double[ma.length][mb[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                double cell = 0;
                for (int k = 0; k < mb.length; k++) {
                    cell += ma[i][k] * mb[k][j];
                }
                result[i][j] = doubleFunction.apply(cell);
            }
        }

        return result;
    }

    public static double[][] transposeFirstAndMultiply(double[][] ma, double[][] mb) {
        checkMultiplyMatrixCompatible(ma, mb);

        double[][] result = new double[ma[0].length][mb[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                double cell = 0;
                for (int k = 0; k < mb.length; k++) {
                    cell += ma[k][i] * mb[k][j];
                }
                result[i][j] = cell;
            }
        }

        return result;
    }

    public static double[][] multiply(double[][] matrix, double scalar) {
        return withScalar(matrix, scalar, Operation.MULTIPLY);
    }

    public static double[][] multiplyByElements(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.MULTIPLY);
    }

    public static double[][] multiplyByElements(@NotNull List<double[][]> matrices,
                                                @NotNull List<@Nullable Function<Double, Double>> functions) {

        if (matrices.isEmpty()) {
            throw new IllegalArgumentException("Empty matrices list");
        }

        if (matrices.size() != functions.size()) {
            throw new IllegalArgumentException("Wrong function list");
        }

        double[][] first = matrices.getFirst();
        double[][] result = new double[first.length][first[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                double cell = 1;
                for (int k = 0; k < matrices.size(); k++) {
                    Function<Double, Double> doubleFunction = functions.get(k);
                    cell *= doubleFunction == null
                            ? matrices.get(k)[i][j]
                            : doubleFunction.apply(matrices.get(k)[i][j]);
                }
                result[i][j] = cell;
            }
        }
        return result;
    }

    public static double[][] divide(double[][] matrix, double scalar) {
        return withScalar(matrix, scalar, Operation.DIVIDE);
    }

    public static double[][] add(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.ADD);
    }

    public static double[][] add(double[][] matrix, double scalar) {
        return withScalar(matrix, scalar, Operation.ADD);
    }

    public static double[][] subtract(double[][] ma, double[][] mb) {
        return byElements(ma, mb, Operation.SUBTRACT);
    }

    public static double[][] subtract(double scalar, double[][] matrix) {
        return withScalar(matrix, scalar, Operation.SUBTRACT_FROM_SCALAR);
    }

    public static double[][] subtract(double[][] matrix, double scalar) {
        return withScalar(matrix, scalar, Operation.SUBTRACT);
    }

    public static double[][] transformToMatrix(double[] array) {
        double[][] matrix = new double[array.length][1];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i][0] = array[i];
        }

        return matrix;
    }

    public static double[][] applyFunction(double[][] matrix, @NotNull DoubleFunction<Double> doubleFunction) {
        double[][] result = new double[matrix.length][matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = doubleFunction.apply(matrix[i][j]);
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
                    case SUBTRACT_FROM_SCALAR ->
                            throw new UnsupportedOperationException(
                                    "This operation work only with scalar");
                };
            }
        }

        return result;
    }

    public static double[][] withScalar(double[][] matrix, double scalar, @NotNull Operation operation) {
        checkEmpty(matrix);

        if (operation == Operation.DIVIDE) {
            if (scalar == 0) {
                throw new IllegalArgumentException("Can not divide by zero");
            }
        }

        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i][j] = switch (operation) {
                    case ADD -> matrix[i][j] + scalar;
                    case SUBTRACT -> matrix[i][j] - scalar;
                    case SUBTRACT_FROM_SCALAR -> scalar - matrix[i][j];
                    case MULTIPLY -> matrix[i][j] * scalar;
                    case DIVIDE -> matrix[i][j] / scalar;
                };
            }
        }

        return result;
    }

    public static double min(double[][] matrix) {
        return minmax(matrix)[0];
    }

    public static double max(double[][] matrix) {
        return minmax(matrix)[1];
    }

    public static double[] minmax(double[][] matrix) {
        checkEmpty(matrix);
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double[] row : matrix) {
            for (double v : row) {
                if (v < min) {
                    min = v;
                }

                if (v > max) {
                    max = v;
                }
            }
        }
        return new double[] {min, max};
    }

    public static String toString(double[][] matrix) {
        StringBuilder result = new StringBuilder();
        for (double[] row : matrix) {
            for (double v : row) {
                result.append(v).append(",");
            }
        }

        result.delete(result.length() - 1, result.length());
        return result.toString();
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

    public static void checkElementsOperationCompatible(double[][] ma, double[][] mb) {
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

    public enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, SUBTRACT_FROM_SCALAR
    }
}
