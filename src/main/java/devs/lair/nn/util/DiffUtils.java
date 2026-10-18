package devs.lair.nn.util;

import java.util.Arrays;

public class DiffUtils {
    public static int calculateLevenshtein(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

    public static double min(double... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Double.MAX_VALUE);
    }

    public static int calculateArrayDiff(int[] etalon, int[] compared) {
        int diff = 0;
        for (int i = 0; i < etalon.length; i++) {
            diff += Math.abs(etalon[i] - compared[i]);
        }

        return diff;
    }

    public static double calculateArrayDiff(double[] etalon, double[] compared) {
        double diff = 0;
        for (int i = 0; i < etalon.length; i++) {
            diff += Math.abs(etalon[i] - compared[i]);
        }

        return diff;
    }

    //TODO : to double
    public static int calculateEuclidDistance(int[] etalon, int[] compared) {
        int diff = 0;

        for (int i = 0; i < etalon.length; i++) {
            diff += (int) Math.pow(etalon[i] - compared[i], 2);
        }

        return (int) Math.pow(diff, 0.5);
    }

    public static double calculateEuclidDistance(double[] etalon, double[] compared) {
        double diff = 0;

        for (int i = 0; i < etalon.length; i++) {
            diff += Math.pow(etalon[i] - compared[i], 2);
        }

        return Math.pow(diff, 0.5);
    }

    public static int[] calculateArrayDiffWithPixelCount(int[] etalon, int[] compared) {
        int diff = 0;
        int pixelCount = 0;
        for (int i = 0; i < etalon.length; i++) {
            int pixelDiff = Math.abs(etalon[i] - compared[i]);
            if (pixelDiff != 0) {
                diff += pixelDiff;
                pixelCount++;
            }
        }

        return new int[]{diff, pixelCount, diff / pixelCount};
    }
}