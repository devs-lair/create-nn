package devs.lair.nn.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MnistCsvViewer extends JPanel {
    private final static int GRID_COLUMNS_COUNT = 25;

    private final JFrame frame;
    private final JLabel fileNameLabel;
    private final JPanel gridPanel;
    private final JButton chooseFileButton;

    private JFileChooser fc;

    public MnistCsvViewer(@NotNull JFrame frame) {
        this.frame = frame;
        this.fileNameLabel = new JLabel("Select file");
        this.gridPanel = new JPanel(new GridLayout(0, GRID_COLUMNS_COUNT));
        this.chooseFileButton = new JButton("Select file");

        setLayout(new BorderLayout());
        add(createChooseFilePanel(), BorderLayout.NORTH);
        add(createGridScrollPane(), BorderLayout.CENTER);

        tryLoadDefault();
    }

    private @NotNull JComponent createGridScrollPane() {
        return new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private @NotNull JComponent createChooseFilePanel() {
        chooseFileButton.addActionListener(_-> {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.addChoosableFileFilter(new CsvFilter());
            fc.setAcceptAllFileFilterUsed(false);

            if (fc.showDialog(this, "Select")
                    == JFileChooser.APPROVE_OPTION) {
                onFileSelect(Paths.get(fc.getSelectedFile().getPath()));
            }

            fc.setSelectedFile(null);
        });

        JPanel choosePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        choosePanel.add(fileNameLabel);
        choosePanel.add(chooseFileButton);
        return choosePanel;
    }

    private void tryLoadDefault() {
        URL defaultUrl = MnistCsvViewer.class.getResource("/mnist/mnist_train_100.csv");

        if (defaultUrl != null) {
            onFileSelect(Paths.get(defaultUrl.getFile()));
        }
    }

    private void onFileSelect(@NotNull Path path) {
        fileNameLabel.setText(path.getFileName().toString());
        gridPanel.removeAll();

        String line;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                JLabel jLabel = createNumberLabel(split);
                gridPanel.add(jLabel);
            }
        } catch (Exception e) {
            gridPanel.removeAll();
            JOptionPane.showMessageDialog(frame, "Wrong CSV Fils", "Import Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        gridPanel.repaint();
    }

    private static @NotNull JLabel createNumberLabel(String[] split) {
        if ((split.length == 0) || (split.length - 1) % 2 != 0) {
            throw new IllegalArgumentException("Wrong CSV file");
        }

        int size = (int) Math.pow((split.length - 1), 0.5);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        DataBuffer dataBuffer = image.getRaster().getDataBuffer();

        for (int i = 1; i < split.length ; i++) {
            dataBuffer.setElem(i - 1, Integer.parseInt(split[i]));
        }

        JLabel jLabel = new JLabel(split[0]);
        jLabel.setIcon(new ImageIcon(image));
        jLabel.setHorizontalTextPosition(JLabel.CENTER);
        jLabel.setVerticalTextPosition(JLabel.BOTTOM);
        return jLabel;
    }

    public static @Nullable String getExtension(@NotNull File f) {
        int i = f.getName().lastIndexOf('.');
        if (i > 0 && i < f.getName().length() - 1) {
            return f.getName().substring(i + 1).toLowerCase();
        }
        return null;
    }

    private static class CsvFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || "csv".equals(getExtension(file));
        }

        @Override
        public String getDescription() {
            return "CSV file";
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MnistCsvViewer::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("Mnist Data Set Viewer");
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setSize(800, 350);
        f.getContentPane().add(new MnistCsvViewer(f));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
