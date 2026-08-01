package devs.lair.nn.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

class CsvFilter extends FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory() || "csv".equals(MnistCsvViewer.getExtension(file));
    }

    @Override
    public String getDescription() {
        return "CSV file";
    }
}
