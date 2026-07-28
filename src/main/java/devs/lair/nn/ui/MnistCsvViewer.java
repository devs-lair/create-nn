package devs.lair.nn.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MnistCsvViewer extends JPanel {
    private final static int GRID_COLUMNS_COUNT = 25;

    private final JFrame frame;
    private final JLabel fileNameLabel;
    private final JPanel gridPanel;
    private final JButton selectFileButton;
    private final ProgressMonitor progressMonitor;
    private final JScrollPane scrollPane;
    private final List<Integer> includeNumbers;
    private final JPanel selectNumbersPanel;

    private JFileChooser fc;
    private Path currentPath;

    public MnistCsvViewer(@NotNull JFrame frame) {
        this.frame = frame;
        this.fileNameLabel = new JLabel("Select file");
        this.gridPanel = new JPanel(new GridLayout(0, GRID_COLUMNS_COUNT));
        this.selectFileButton = new JButton("Select file");
        this.selectNumbersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.scrollPane = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.progressMonitor = new ProgressMonitor(this,
                "Load data from file",
                "", 0, 100);

        this.includeNumbers = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

        setLayout(new BorderLayout());
        add(initSelectFileAndFilterPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        tryLoadDefault();
    }

    private @NotNull JComponent initSelectFileAndFilterPanel() {
        selectFileButton.addActionListener(_ -> {
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

        JPanel selectFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectFilePanel.add(fileNameLabel);
        selectFilePanel.add(selectFileButton);

        initSelectNumberPanel();

        JPanel selectDataPanel = new JPanel(new BorderLayout());
        selectDataPanel.add(selectFilePanel, BorderLayout.NORTH);
        selectDataPanel.add(selectNumbersPanel);

        return selectDataPanel;
    }

    private void initSelectNumberPanel() {

        JCheckBox allCheckbox = new JCheckBox("All", true);
        JCheckBox noneCheckBox = new JCheckBox("None", false);

        allCheckbox.addActionListener(_ -> {
            if (allCheckbox.isSelected()) {
                includeNumbers.clear();
                includeNumbers.addAll(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

                for (int i = 0; i < selectNumbersPanel.getComponentCount(); i++) {
                    Component component = selectNumbersPanel.getComponent(i);
                    if (component instanceof JCheckBox checkBox) {
                        checkBox.setSelected(true);
                    }
                }

                noneCheckBox.setSelected(false);

                if (currentPath != null) {
                    onFileSelect(currentPath);
                }
            }
        });

        noneCheckBox.addActionListener(_ -> {
            if (noneCheckBox.isSelected()) {
                includeNumbers.clear();

                for (int i = 0; i < selectNumbersPanel.getComponentCount(); i++) {
                    Component component = selectNumbersPanel.getComponent(i);
                    if (component instanceof JCheckBox checkBox && checkBox != noneCheckBox) {
                        checkBox.setSelected(false);
                    }
                }

                gridPanel.removeAll();
                gridPanel.repaint();
            }
        });

        for (int i = 0; i < 10; i++) {
            JCheckBox checkBox = new JCheckBox(String.valueOf(i), true);
            checkBox.addActionListener(e -> {
                JCheckBox source = (JCheckBox) e.getSource();
                Integer number = Integer.parseInt(source.getText());
                if (source.isSelected()) {
                    includeNumbers.add(number);
                    noneCheckBox.setSelected(false);
                } else {
                    includeNumbers.remove(number);
                    allCheckbox.setSelected(false);
                }

                if (currentPath != null) {
                    onFileSelect(currentPath);
                }
            });

            selectNumbersPanel.add(checkBox);
        }

        selectNumbersPanel.add(allCheckbox);
        selectNumbersPanel.add(noneCheckBox);
    }

    private void tryLoadDefault() {
        URL defaultUrl = MnistCsvViewer.class.getResource("/mnist/mnist_train_100.csv");

        if (defaultUrl != null) {
            onFileSelect(Paths.get(defaultUrl.getFile()));
        }
    }

    private void onFileSelect(@NotNull Path path) {
        currentPath = path;
        fileNameLabel.setText(path.getFileName().toString());
        gridPanel.removeAll();

        //fast exit
        if (includeNumbers.isEmpty()) {
            gridPanel.repaint();
            return;
        }

        selectFileButton.setEnabled(false);
        setPanelEnabled(selectNumbersPanel, false);
        scrollPane.getViewport().remove(gridPanel);

        createLoadTask().execute();
    }

    private void setPanelEnabled(@NotNull JPanel panel, boolean enabled) {
        for (int i = 0; i < panel.getComponentCount(); i++) {
            if (panel.getComponent(i) instanceof JComponent jComponent) {
                jComponent.setEnabled(enabled);
            }
        }
    }

    private @NotNull LoadFileTask createLoadTask() {
        LoadFileTask loadFileTask = new LoadFileTask();
        loadFileTask.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                Integer progress = (Integer) evt.getNewValue();
                progressMonitor.setProgress(progress);
                if (progress > 95) {
                    progressMonitor.setNote("Load completed. Rendering");
                } else {
                    progressMonitor.setNote(String.format("Completed %d%%.\n", progress));
                }

                if (progressMonitor.isCanceled()) {
                    if (progressMonitor.isCanceled()) {
                        loadFileTask.cancel(true);
                    }
                }
            }
        });

        return loadFileTask;
    }



    private class LoadFileTask extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            setProgress(0);
            String line;

            try (BufferedReader reader = Files.newBufferedReader(currentPath)) {
                long size = Files.size(currentPath);
                long readTotal = 0;
                long chunk = 0;
                while ((line = reader.readLine()) != null && !isCancelled()) {
                    chunk += line.getBytes(StandardCharsets.UTF_8).length;
                    if (chunk > size / 100) {
                        readTotal += chunk;
                        chunk = 0;
                        setProgress((int) ((readTotal / (double) size) * 100));
                    }

                    String[] split = line.split(",");
                    int number = Integer.parseInt(split[0]);
                    if (!includeNumbers.contains(number)) {
                        continue;
                    }

                    JLabel jLabel = createNumberLabel(split);
                    gridPanel.add(jLabel);
                }
            } catch (Exception e) {
                gridPanel.removeAll();
                currentPath = null;
                JOptionPane.showMessageDialog(frame, "Wrong CSV Fils", "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                scrollPane.getViewport().add(gridPanel);
                if (isCancelled()) {
                    currentPath = null;
                    gridPanel.removeAll();
                }

            }

            setProgress(100);
            return null;
        }

        @Override
        public void done() {
            SwingUtilities.invokeLater(() -> {
                if (currentPath == null) {
                    fileNameLabel.setText("Select file");
                }
                selectFileButton.setEnabled(true);
                setPanelEnabled(selectNumbersPanel, true);
            });
        }

        private @NotNull JLabel createNumberLabel(String[] split) {
            if ((split.length == 0) || (split.length - 1) % 2 != 0) {
                throw new IllegalArgumentException("Wrong CSV file");
            }

            int size = (int) Math.pow((split.length - 1), 0.5);
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
            DataBuffer dataBuffer = image.getRaster().getDataBuffer();

            for (int i = 1; i < split.length; i++) {
                dataBuffer.setElem(i - 1, 255 - Integer.parseInt(split[i]));
            }

            JLabel jLabel = new JLabel(split[0]);
            jLabel.setIcon(new ImageIcon(image));
            jLabel.setHorizontalTextPosition(JLabel.CENTER);
            jLabel.setVerticalTextPosition(JLabel.BOTTOM);
            jLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Image resultingImage = image.getScaledInstance(size * 10, size * 10, Image.SCALE_DEFAULT);
                    BufferedImage outputImage = new BufferedImage(size * 10, size * 10, BufferedImage.TYPE_BYTE_GRAY);
                    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

                    final JDialog dialog = new JDialog(MnistCsvViewer.this.frame, "Number %s".formatted(split[0]), true);
                    dialog.getContentPane().add(new JLabel(new ImageIcon(outputImage)));
                    dialog.pack();
                    dialog.setLocationRelativeTo(MnistCsvViewer.this.frame);
                    dialog.setVisible(true);
                }
            });

            return jLabel;
        }
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
