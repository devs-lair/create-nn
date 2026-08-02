package devs.lair.nn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatrixUtilsTest {

    @Test
    @DisplayName("Transpose Matrix")
    void transposeMatrixTest() {
        double[][] matrix = new double[][]{{1, 1, 1}, {2, 2, 2}, {3, 3, 3}};
        double[][] transposed = MatrixUtils.transpose(matrix);

        assertThat(transposed).hasDimensions(3, 3);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[0][1]).isEqualTo(2);
        assertThat(transposed[0][2]).isEqualTo(3);
        assertThat(transposed[1][0]).isEqualTo(1);
        assertThat(transposed[1][1]).isEqualTo(2);
        assertThat(transposed[1][2]).isEqualTo(3);
        assertThat(transposed[2][0]).isEqualTo(1);
        assertThat(transposed[2][1]).isEqualTo(2);
        assertThat(transposed[2][2]).isEqualTo(3);
    }

    @Test
    @DisplayName("Transpose One Row Matrix")
    void transposeOnwRowTest() {
        double[][] matrix = new double[][]{{1, 2, 3}};
        double[][] transposed = MatrixUtils.transpose(matrix);

        assertThat(transposed).hasDimensions(3, 1);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[1][0]).isEqualTo(2);
        assertThat(transposed[2][0]).isEqualTo(3);
    }

    @Test
    @DisplayName("Transpose Array")
    void transposeArrayTest() {
        double[] matrix = new double[]{1, 2, 3};
        double[][] transposed = MatrixUtils.transpose(matrix);

        assertThat(transposed).hasDimensions(3, 1);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[1][0]).isEqualTo(2);
        assertThat(transposed[2][0]).isEqualTo(3);
    }

    @Test
    @DisplayName("Transpose One Column Matrix")
    void transposeOneColumnTest() {
        double[][] matrix = new double[][]{{1}, {2}, {3}};
        double[][] transposed = MatrixUtils.transpose(matrix);

        assertThat(transposed).hasDimensions(1, 3);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[0][1]).isEqualTo(2);
        assertThat(transposed[0][2]).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiply to scalar")
    void multiplyToScalarTest() {
        double[][] matrix = new double[][]{{1, 2}, {3, 4}};

        double[][] withScalar = MatrixUtils.multiply(matrix, 2);
        assertThat(withScalar).hasDimensions(2, 2);

        assertThat(withScalar[0][0]).isEqualTo(2);
        assertThat(withScalar[0][1]).isEqualTo(4);
        assertThat(withScalar[1][0]).isEqualTo(6);
        assertThat(withScalar[1][1]).isEqualTo(8);
    }

    @Test
    @DisplayName("Multiply matrix")
    void multiplyMatrixTest() {
        double[][] ma = new double[][]{{1, 1}, {1, 1}};
        double[][] mb = new double[][]{{2, 2}, {2, 2}};
        double[][] mc = MatrixUtils.multiply(ma, mb);

        assertThat(mc).hasDimensions(2, 2);

        assertThat(mc[0][0]).isEqualTo(4);
        assertThat(mc[0][1]).isEqualTo(4);
        assertThat(mc[1][0]).isEqualTo(4);
        assertThat(mc[1][1]).isEqualTo(4);

        //second try
        ma = new double[][]{{1, 1}, {1, 1}};
        mb = new double[][]{{1, 1}, {1, 1}};
        mc = MatrixUtils.multiply(ma, mb);

        assertThat(mc).hasDimensions(2, 2);

        assertThat(mc[0][0]).isEqualTo(2);
        assertThat(mc[0][1]).isEqualTo(2);
        assertThat(mc[1][0]).isEqualTo(2);
        assertThat(mc[1][1]).isEqualTo(2);

        //third
        ma = new double[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
        mb = new double[][]{{2}, {2}, {2}};
        mc = MatrixUtils.multiply(ma, mb);

        assertThat(mc).hasDimensions(3, 1);

        assertThat(mc[0][0]).isEqualTo(6);
        assertThat(mc[1][0]).isEqualTo(6);
        assertThat(mc[2][0]).isEqualTo(6);

        //fourth, square matrix
        ma = new double[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
        mb = new double[][]{{2, 2, 2}, {2, 2, 2}, {2, 2, 2}};
        mc = MatrixUtils.multiply(ma, mb);

        assertThat(mc).hasDimensions(3, 3);

        for (double[] doubles : mc) {
            for (int j = 0; j < mc[0].length; j++) {
                assertThat(doubles[j]).isEqualTo(6);
            }
        }
    }

    @Test
    @DisplayName("Transform to matrix test")
    void transformToMatrix() {
        double[] array = new double[]{1, 2, 3};
        double[][] matrix = MatrixUtils.transformToMatrix(array);

        assertThat(matrix).hasDimensions(3, 1);

        assertThat(matrix[0][0]).isEqualTo(1);
        assertThat(matrix[1][0]).isEqualTo(2);
        assertThat(matrix[2][0]).isEqualTo(3);
    }

    @Test
    @DisplayName("Apply function to matrix")
    void applyFunction() {
        double[][] matrix = new double[][]{{2, 2}, {3, 3}};

        double[][] applied = MatrixUtils.applyFunction(matrix, (x) -> x * 2);
        assertThat(applied).hasDimensions(2, 2);

        assertThat(applied[0][0]).isEqualTo(4);
        assertThat(applied[0][1]).isEqualTo(4);
        assertThat(applied[1][0]).isEqualTo(6);
        assertThat(applied[1][1]).isEqualTo(6);
    }

    @Test
    @DisplayName("Subtract matrix")
    void subtractMatrix() {
        double[][] a = new double[][] {{3, 3},{2, 2},{1, 1}};
        double[][] b = new double[][] {{1, 1},{1, 1},{1, 1}};
        double[][] c = MatrixUtils.subtract(a, b);

        assertThat(c).hasDimensions(3, 2);

        assertThat(c[0][0]).isEqualTo(2);
        assertThat(c[0][1]).isEqualTo(2);
        assertThat(c[1][0]).isEqualTo(1);
        assertThat(c[1][1]).isEqualTo(1);
        assertThat(c[2][0]).isEqualTo(0);
        assertThat(c[2][1]).isEqualTo(0);

        assertThatThrownBy(() -> MatrixUtils.subtract(new double[][] {{1}, {2}},
                        new double[][] {{1}, {2}, {3}}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Add matrix")
    void addMatrix() {
        double[][] a = new double[][] {{3, 3},{2, 2},{1, 1}};
        double[][] b = new double[][] {{1, 1},{1, 1},{1, 1}};
        double[][] c = MatrixUtils.add(a, b);

        assertThat(c).hasDimensions(3, 2);

        assertThat(c[0][0]).isEqualTo(4);
        assertThat(c[0][1]).isEqualTo(4);
        assertThat(c[1][0]).isEqualTo(3);
        assertThat(c[1][1]).isEqualTo(3);
        assertThat(c[2][0]).isEqualTo(2);
        assertThat(c[2][1]).isEqualTo(2);

        assertThatThrownBy(() -> MatrixUtils.add(new double[][] {{1}, {2}},
                new double[][] {{1}, {2}, {3}}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Subtract from scalar")
    void subtractFromScalar() {
        double[][] a = new double[][] {{0.5, 0.5},{1, 1}};
        double[][] c = MatrixUtils.subtract(1, a);

        assertThat(c).hasDimensions(2, 2);

        assertThat(c[0][0]).isEqualTo(0.5);
        assertThat(c[0][1]).isEqualTo(0.5);
        assertThat(c[1][0]).isEqualTo(0);
        assertThat(c[1][1]).isEqualTo(0);
    }

    //==== Negative Test ====

    @Test
    @DisplayName("Check empty")
    void checkEmptyTest() {
        double[][] empty = new double[0][0];
        assertThatThrownBy(() -> MatrixUtils.checkEmpty(empty))
                .isInstanceOf(IllegalArgumentException.class);

        double[] emptyArray = new double[0];
        assertThatThrownBy(() -> MatrixUtils.checkEmpty(emptyArray))
                .isInstanceOf(IllegalArgumentException.class);

        double[][] motConstantLength = new double[][]{{1, 2, 3}, {3}, {2, 3}};
        assertThatThrownBy(() -> MatrixUtils.checkConstantLength(motConstantLength))
                .isInstanceOf(IllegalArgumentException.class);

        double[][] emtpyCols = new double[][]{{}, {}, {}};
        assertThatThrownBy(() -> MatrixUtils.checkConstantLength(emtpyCols))
                .isInstanceOf(IllegalArgumentException.class);

        double[][] ma = new double[][]{{2, 2}, {1, 1}};
        double[][] mb = new double[][]{{1, 2}};

        assertThatThrownBy(() -> MatrixUtils.checkMultiplyMatrixCompatible(ma, mb))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Private constructor call (coverage test")
    void callPrivateConstructorTest() throws NoSuchMethodException {
        Constructor<MatrixUtils> pcc = MatrixUtils.class.getDeclaredConstructor();
        pcc.setAccessible(true);

        assertThatThrownBy(pcc::newInstance)
                .isInstanceOf(InvocationTargetException.class);

    }
}