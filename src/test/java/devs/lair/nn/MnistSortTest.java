package devs.lair.nn;

import devs.lair.nn.ui.MnistCsvViewer;
import devs.lair.nn.util.DiffUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MnistSortTest {

    @Test
    @DisplayName("Sort mnist dataset by count of pixels")
    @Disabled("manual run")
    void sortMnistDatasetByPixels() {
        URL file = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(file).isNotNull();

        List<int[]> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file.getFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");

                int[] record = convertLineToInputArray(split);
                if (record[0] == 0) {
                    records.add(record);
                }
            }

            List<String> strings = records.stream().sorted(byPixelComparator()).map(MnistSortTest::arrayToString).toList();

            File forSave = new File("mnist-sorted-0.csv");
            if (!forSave.exists()) {
                Files.createFile(forSave.toPath());
            }

            Files.write(forSave.toPath(), strings, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            //ignored
        }
    }


    @Test
    @DisplayName("Sort mnist dataset by Levenshtein scale ")
    @Disabled("for hand run")
    void sortMnistDatasetLevenshtein() throws IOException {
        URL file = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(file).isNotNull();

        List<int[]> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file.getFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");

                int[] record = convertLineToInputArray(split);
                if (record[0] == 0) {
                    records.add(record);
                }
            }

            List<String> strings = records.stream().sorted(byPixelComparator()).
                    map(MnistSortTest::arrayToString).toList();

            List<DiffRecord> diffs = new ArrayList<>();

            String first = strings.getFirst();
            first = first.substring(1, first.length() - 1);

            for (int i = 1; i < strings.size(); i++) {
                System.out.println(i);
                String row = strings.get(i);
                row = row.substring(1, row.length() - 1);
                diffs.add(new DiffRecord(DiffUtils.calculateLevenshtein(first, row), i));
            }

            diffs = diffs.stream().sorted(Comparator.comparingInt(DiffRecord::diff)).toList();

            List<String> reorder = new ArrayList<>();
            reorder.add(strings.getFirst());
            for (DiffRecord diff : diffs) {
                reorder.add(strings.get(diff.index()));
            }

            File forSave = new File("mnist-sorted-0.csv");
            if (!forSave.exists()) {
                Files.createFile(forSave.toPath());
            }

            Files.write(forSave.toPath(), reorder, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9})
    @Disabled("Manual run")
    @DisplayName("Sort by my diffs")
    void sortBySpecialDiff(int number) throws IOException {
        URL file = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(file).isNotNull();

        List<int[]> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file.getFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");

                int[] record = convertLineToInputArray(split);
                if (record[0] == number) {
                    records.add(record);
                }
            }
        }

        records = records.stream().sorted(byPixelComparator()).toList();

        List<Integer> used = new ArrayList<>();
        List<DiffRecord> diffs = new ArrayList<>();
        List<int[]> reorder = new ArrayList<>();
        reorder.add(records.getFirst());
        used.add(0);
        int[] etalon = records.getFirst();

        for (int i = 1; i < records.size(); i++) {
            diffs.clear();
            for (int j = 1; j < records.size(); j++) {
                if (used.contains(j)) {
                    continue;
                }
                diffs.add(new DiffRecord(calculateArrayDiff(etalon, records.get(j)), j));
            }

            List<DiffRecord> sorted = diffs.stream()
                    .sorted(Comparator.comparingInt(DiffRecord::diff)).toList();

            if (!sorted.isEmpty()) {
                int[] same = records.get(sorted.getFirst().index());
                used.add(sorted.getFirst().index());
                reorder.add(same);
                etalon = same;
            } else {
                //it's last
                reorder.add(etalon);
            }
        }

        File forSave = new File("mnist-sorted-my-diff-" + number + ".csv");

        if (!forSave.exists()) {
            Files.createFile(forSave.toPath());
        }

        Files.write(forSave.toPath(),
                reorder.stream().map(MnistSortTest::arrayToString).toList(),
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    @DisplayName("Find groups")
    @Disabled("Manual run")
    void findAndReduceGroups() throws IOException {
        List<int[]> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of("mnist-sorted-my-diff-0.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                records.add(convertLineToInputArray(split));
            }
        }

        List<int[]> diffs = new ArrayList<>();

        for (int i = 0; i < records.size() - 1; i++) {
            int[] first = records.get(i);
            int[] second = records.get(i + 1);
            diffs.add(calculateArrayDiffWithPixelCount(first, second));
        }

        List<int[]> groups = new ArrayList<>();
        groups.add(records.getFirst());
        for (int i = 0; i < diffs.size(); i++) {
            int[] diff = diffs.get(i);
            if (diff[2] > 70) {
                groups.add(records.get(i+1));
            }
        }

        File forSave = new File("mnist-reduced-0.csv");

        if (!forSave.exists()) {
            Files.createFile(forSave.toPath());
        }

        Files.write(forSave.toPath(),
                groups.stream().map(MnistSortTest::arrayToString).toList(),
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static @NotNull Comparator<int[]> byPixelComparator() {
        return (o1, o2) -> {
            int o1count = 0;
            for (int v : o1) {
                if (v > 0) {
                    o1count++;
                }
            }
            int o2count = 0;
            for (int v : o2) {
                if (v > 0) {
                    o2count++;
                }
            }

            return Integer.compare(o1count, o2count);
        };
    }

    private static @NotNull String arrayToString(int[] d) {
        StringBuilder sb = new StringBuilder();
        for (int v : d) {
            sb.append(v).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    private int calculateArrayDiff(int[] etalon, int[] compared) {
        int diff = 0;
        for (int i = 0; i < etalon.length; i++) {
            diff += Math.abs(etalon[i] - compared[i]);
        }

        return diff;
    }

    private int[] calculateArrayDiffWithPixelCount(int[] etalon, int[] compared) {
        int diff = 0;
        int pixelCount = 0;
        for (int i = 0; i < etalon.length; i++) {
            int pixelDiff = Math.abs(etalon[i] - compared[i]);
            if (pixelDiff != 0) {
                diff += pixelDiff;
                pixelCount++;
            }
        }

        return new int[]{diff, pixelCount, diff/pixelCount};
    }


    private static int[] convertLineToInputArray(String[] split) {
        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }

    private record DiffRecord(int diff, int index) {
    }
}
