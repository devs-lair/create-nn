package devs.lair.nn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

public class Nd4jTest {
    @Test
    @DisplayName("Transpose Matrix")
    void transposeMatrixTest() {
        INDArray matrix = Nd4j.create(new double[][]{{1, 1, 1}, {2, 2, 2}, {3, 3, 3}});
        double[][] transposed = matrix.transpose().dup().toDoubleMatrix();

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

        matrix.close();
    }

    @Test
    @DisplayName("Transpose One Row Matrix")
    void transposeOnwRowTest() {
        INDArray matrix = Nd4j.create(new double[][]{{1, 2, 3}});
        double[][] transposed = matrix.transpose().toDoubleMatrix();

        assertThat(transposed).hasDimensions(3, 1);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[1][0]).isEqualTo(2);
        assertThat(transposed[2][0]).isEqualTo(3);

        //original matrix has same shape
        assertThat(matrix.shape()).hasSize(2);
        assertThat(matrix.shape()[0]).isEqualTo(1);
        assertThat(matrix.shape()[1]).isEqualTo(3);

        matrix.close();
    }

    @Test
    @DisplayName("Transpose Array, negative test")
    void transposeArrayTest() {
        INDArray matrix = Nd4j.create(new double[]{1, 2, 3});
        assertThatThrownBy(matrix::transpose).isInstanceOf(IllegalStateException.class);

        matrix.close();
    }

    @Test
    @DisplayName("Transpose One Column Matrix")
    void transposeOneColumnTest() {
        INDArray matrix = Nd4j.create(new double[][]{{1}, {2}, {3}});
        double[][] transposed = matrix.transpose().toDoubleMatrix();

        assertThat(transposed).hasDimensions(1, 3);
        assertThat(transposed[0][0]).isEqualTo(1);
        assertThat(transposed[0][1]).isEqualTo(2);
        assertThat(transposed[0][2]).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiply to scalar")
    void multiplyToScalarTest() {
        INDArray matrix  = Nd4j.create(new double[][]{{1, 2}, {3, 4}});

        //new matrix
        double[][] withScalar = matrix.mul(2).toDoubleMatrix();
        assertThat(withScalar).hasDimensions(2, 2);
        assertThat(withScalar[0][0]).isEqualTo(2);
        assertThat(withScalar[0][1]).isEqualTo(4);
        assertThat(withScalar[1][0]).isEqualTo(6);
        assertThat(withScalar[1][1]).isEqualTo(8);

        //inplace
        double[][] doubleMatrix = matrix.muli(2).toDoubleMatrix();
        assertThat(doubleMatrix).hasDimensions(2, 2);
        assertThat(doubleMatrix[0][0]).isEqualTo(2);
        assertThat(doubleMatrix[0][1]).isEqualTo(4);
        assertThat(doubleMatrix[1][0]).isEqualTo(6);
        assertThat(doubleMatrix[1][1]).isEqualTo(8);
    }

    @Test
    @DisplayName("Multiply matrix")
    void multiplyMatrixTest() {
        INDArray ma = Nd4j.create(new double[][]{{1, 1}, {1, 1}});
        INDArray mb = Nd4j.create(new double[][]{{2, 2}, {2, 2}});
        double[][] mc = ma.mmul(mb).toDoubleMatrix();

        assertThat(mc).hasDimensions(2, 2);

        assertThat(mc[0][0]).isEqualTo(4);
        assertThat(mc[0][1]).isEqualTo(4);
        assertThat(mc[1][0]).isEqualTo(4);
        assertThat(mc[1][1]).isEqualTo(4);

        //second try
        ma = Nd4j.create(new double[][]{{1, 1}, {1, 1}});
        mb = Nd4j.create(new double[][]{{1, 1}, {1, 1}});
        mc = ma.mmul(ma, mb).toDoubleMatrix();

        assertThat(mc).hasDimensions(2, 2);

        assertThat(mc[0][0]).isEqualTo(2);
        assertThat(mc[0][1]).isEqualTo(2);
        assertThat(mc[1][0]).isEqualTo(2);
        assertThat(mc[1][1]).isEqualTo(2);

        //third
        ma = Nd4j.create(new double[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}});
        mb = Nd4j.create(new double[][]{{2}, {2}, {2}});
        mc = ma.mmul(mb).toDoubleMatrix();

        assertThat(mc).hasDimensions(3, 1);

        assertThat(mc[0][0]).isEqualTo(6);
        assertThat(mc[1][0]).isEqualTo(6);
        assertThat(mc[2][0]).isEqualTo(6);
    }


    @Test
    @DisplayName("Apply sigmoid to matrix")
    void applyFunctionSigmoid() {
        INDArray matrix =  Nd4j.create(new double[][]{{1, 1}, {2, 2}});
        double[][] applied = Transforms.sigmoid(matrix, false).toDoubleMatrix();
        assertThat(applied).hasDimensions(2, 2);
        assertThat(applied[0][0]).isCloseTo(0.7310585786300049, within(0.0001));
        assertThat(applied[0][1]).isCloseTo(0.7310585786300049, within(0.0001));
        assertThat(applied[1][0]).isCloseTo(0.8807970779778823, within(0.0001));
        assertThat(applied[1][1]).isCloseTo(0.8807970779778823, within(0.0001));
    }
}
