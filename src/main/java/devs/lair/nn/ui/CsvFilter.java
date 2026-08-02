package devs.lair.nn.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class CsvFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        return file.isDirectory() || "csv".equals(getExtension(file));
    }

    @Override
    public String getDescription() {
        return "CSV file";
    }

    private @Nullable String getExtension(@NotNull File f) {
        int i = f.getName().lastIndexOf('.');
        if (i > 0 && i < f.getName().length() - 1) {
            return f.getName().substring(i + 1).toLowerCase();
        }

        return null;
    }
}
