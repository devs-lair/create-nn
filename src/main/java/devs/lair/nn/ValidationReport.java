package devs.lair.nn;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class ValidationReport {
    private final int totalRecordsCount;
    private final int totalCorrectCount;
    private final List<String> incorrectRecords;
    private final Duration duration;
    private final Path filePath;
    private final NeuralNetwork neuralNetwork;

    public ValidationReport(int totalCountRecords,
                            int totalCountCorrect,
                            @NotNull List<String> wrongRecords,
                            @NotNull Duration duration,
                            @NotNull Path filePath,
                            @NotNull NeuralNetwork neuralNetwork) {

        this.totalRecordsCount = totalCountRecords;
        this.totalCorrectCount = totalCountCorrect;
        this.incorrectRecords = wrongRecords;
        this.duration = duration;
        this.filePath = filePath;
        this.neuralNetwork = neuralNetwork;
    }

    public int getTotalRecordsCount() {
        return totalRecordsCount;
    }

    public int getTotalCorrectCount() {
        return totalCorrectCount;
    }

    public List<String> getIncorrectRecords() {
        return incorrectRecords;
    }

    public Duration getDuration() {
        return duration;
    }

    public Path getFilePath() {
        return filePath;
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public double getPerformance() {
        return totalCorrectCount / (double) totalRecordsCount;
    }
}
