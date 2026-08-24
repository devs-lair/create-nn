package devs.lair.nn;

import org.apache.commons.math3.util.FastMath;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

public class EjmlTest {

    @Test
    @DisplayName("Transpose Matrix")
    void transposeMatrixTest() {

        SimpleMatrix matrix = new SimpleMatrix(new double[][]{{1, 1, 1}, {2, 2, 2}, {3, 3, 3}});
        double[][] transposed = matrix.transpose().toArray2();

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
        SimpleMatrix matrix = new SimpleMatrix(new double[][]{{1, 2, 3}});
        double[][] transposed = matrix.transpose().toArray2();

        assertThat(transposed).hasDimensions(3, 1);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[1][0]).isEqualTo(2);
        assertThat(transposed[2][0]).isEqualTo(3);
    }

    @Test
    @DisplayName("Transpose Array")
    void transposeArrayTest() {
        SimpleMatrix matrix = new SimpleMatrix(new double[]{1, 2, 3});
        double[][] transposed = matrix.transpose().toArray2();

        assertThat(transposed).hasDimensions(1, 3);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[0][1]).isEqualTo(2);
        assertThat(transposed[0][2]).isEqualTo(3);
    }

    @Test
    @DisplayName("Transpose One Column Matrix")
    void transposeOneColumnTest() {
        SimpleMatrix matrix = new SimpleMatrix(new double[][]{{1}, {2}, {3}});
        double[][] transposed = matrix.transpose().toArray2();

        assertThat(transposed).hasDimensions(1, 3);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[0][1]).isEqualTo(2);
        assertThat(transposed[0][2]).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiply to scalar")
    void multiplyToScalarTest() {
        SimpleMatrix matrix = new SimpleMatrix(new double[][]{{1, 2}, {3, 4}});

        //new matrix
        double[][] withScalar = matrix.scale(2).toArray2();
        assertThat(withScalar).hasDimensions(2, 2);
        assertThat(withScalar[0][0]).isEqualTo(2);
        assertThat(withScalar[0][1]).isEqualTo(4);
        assertThat(withScalar[1][0]).isEqualTo(6);
        assertThat(withScalar[1][1]).isEqualTo(8);
    }

    @Test
    @DisplayName("Multiply matrix")
    void multiplyMatrixTest() {
        SimpleMatrix ma = new SimpleMatrix(new double[][]{{1, 1}, {1, 1}});
        SimpleMatrix mb = new SimpleMatrix(new double[][]{{2, 2}, {2, 2}});
        double[][] mc = ma.mult(mb).toArray2();

        assertThat(mc).hasDimensions(2, 2);

        assertThat(mc[0][0]).isEqualTo(4);
        assertThat(mc[0][1]).isEqualTo(4);
        assertThat(mc[1][0]).isEqualTo(4);
        assertThat(mc[1][1]).isEqualTo(4);

        //second try
        ma = new SimpleMatrix(new double[][]{{1, 1}, {1, 1}});
        mb = new SimpleMatrix(new double[][]{{1, 1}, {1, 1}});
        mc = ma.mult(mb).toArray2();

        assertThat(mc).hasDimensions(2, 2);

        assertThat(mc[0][0]).isEqualTo(2);
        assertThat(mc[0][1]).isEqualTo(2);
        assertThat(mc[1][0]).isEqualTo(2);
        assertThat(mc[1][1]).isEqualTo(2);

        //third
        ma = new SimpleMatrix(new double[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}});
        mb = new SimpleMatrix(new double[][]{{2}, {2}, {2}});
        mc = ma.mult(mb).toArray2();

        assertThat(mc).hasDimensions(3, 1);

        assertThat(mc[0][0]).isEqualTo(6);
        assertThat(mc[1][0]).isEqualTo(6);
        assertThat(mc[2][0]).isEqualTo(6);
    }

    @Test
    @DisplayName("Apply sigmoid to matrix")
    void applyFunctionSigmoid() {
        SimpleMatrix ma = new SimpleMatrix(new double[][]{{1, 1}, {2, 2}});

        double[][] applied = ma.elementOp((row, col, value) -> 1 / (1 + FastMath.exp(-value))).toArray2();

        assertThat(applied).hasDimensions(2, 2);
        assertThat(applied[0][0]).isCloseTo(0.7310585786300049, within(0.0001));
        assertThat(applied[0][1]).isCloseTo(0.7310585786300049, within(0.0001));
        assertThat(applied[1][0]).isCloseTo(0.8807970779778823, within(0.0001));
        assertThat(applied[1][1]).isCloseTo(0.8807970779778823, within(0.0001));
    }
}
