package devs.lair.nn.util;

public class HuMoments {

    private static final double BINARY_THRESHOLD = 0.2d;

    public static double[] hu(int[][] iv) {
        double nu20 = nu(2, 0, iv);
        double nu02 = nu(0, 2, iv);
        double nu11 = nu(1, 1, iv);
        double nu30 = nu(3, 0, iv);
        double nu12 = nu(1, 2, iv);
        double nu21 = nu(2, 1, iv);
        double nu03 = nu(0, 3, iv);

        double h0 = nu20 + nu02;
        double h1 = sq(nu20 - nu02) + 4 * sq(nu11);
        double h2 = sq(nu30 - 3 * nu12) + sq(3 * nu21 - nu03);
        double h3 = sq(nu30 + nu12) + sq(nu21 + nu03);
        double h4 = (nu30 - 3 * nu12) * (nu30 + nu12) * (sq(nu30 + nu12) - 3 * sq(nu21 + nu03))
                + (3 * nu21 - nu03) * (nu21 + nu03) * (3 * sq(nu30 + nu12) - sq(nu21 + nu03));
        double h5 = (nu20 - nu02) * (sq(nu30 + nu12) - sq(nu21 + nu03)) + 4 * nu11 * (nu30 + nu12) * (nu21 + nu03);
        double h6 = (3 * nu21 - nu03) * (nu30 + nu12) * (sq(nu30 + nu12) - 3 * sq(nu21 + nu03))
                - (nu30 - 3 * nu12) * (nu21 + nu03) * (3 * (sq(nu30 + nu12) - sq(nu21 + nu03)));

        double[] result = new double[7];
        result[0] = log(h0);
        result[1] = log(h1);
        result[2] = log(h2);
        result[3] = log(h3);
        result[4] = log(h4);
        result[5] = log(h5);
        result[6] = log(h6);

        return result;
    }

    private static double sq(double base) {
        return Math.pow(base, 2);
    }

    private static double log(double base) {
        return -Math.signum(base) * Math.log10(Math.abs(base));
    }

    private static double moment(int xPow, int yPow, int[][] intensive) {
        double moment = 0;
        for (int x = 0; x < intensive[0].length; x++) {
            double columnSum = 0;
            for (int y = 0; y < intensive.length; y++) {
                columnSum += Math.pow(x, xPow) * Math.pow(y, yPow) * intensive[y][x];
            }
            moment += columnSum;
        }
        return moment;
    }

    private static double centralMoment(int xPow, int yPow, int[][] intensive) {
        double xc = moment(1, 0, intensive) / moment(0, 0, intensive);
        double yc = moment(0, 1, intensive) / moment(0, 0, intensive);

        double centralMoment = 0;
        for (int x = 0; x < intensive[0].length; x++) {
            double columnSum = 0;
            for (int y = 0; y < intensive.length; y++) {
                columnSum += Math.pow(x - xc, xPow) * Math.pow(y - yc, yPow) * intensive[y][x];
            }
            centralMoment += columnSum;
        }
        return centralMoment;
    }

    public static double[] normalize(int[] input) {
        double[] result = new double[input.length - 1];
        for (int i = 1; i < input.length; i++) {
            result[i - 1] = (double) input[i] / 255;
        }

        return result;
    }

    public static int[][] getIntensive(double[] inputs) {
        int size = (int) Math.sqrt(inputs.length);
        int[][] intensive = new int[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                intensive[y][x] = inputs[y * size + x] > BINARY_THRESHOLD ? 255 : 0;
            }
        }

        return intensive;
    }

    private static double nu(int xPow, int yPow, int[][] intensive) {
        return centralMoment(xPow, yPow, intensive) / Math.pow(centralMoment(0, 0, intensive), (xPow + yPow) / 2.0 + 1);
    }
}
