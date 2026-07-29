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
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MnistCsvViewer extends JPanel {

    private final JFrame frame;
    private final JPanel gridPanel;
    private final JPanel selectNumbersPanel;
    private final JLabel fileNameLabel;
    private final JLabel totalNumberAddedLabel;
    private final JButton selectFileButton;
    private final ProgressMonitor progressMonitor;

    private final List<Integer> includeNumbers;

    private JFileChooser fc;
    private Path currentPath;

    public MnistCsvViewer(@NotNull JFrame frame) {
        this.frame = frame;

        this.fileNameLabel = new JLabel("no file selected");
        this.fileNameLabel.setName(FILE_NAME_LABEL_NAME);

        this.totalNumberAddedLabel = new JLabel(TOTAL_NUMBER_TEXT.formatted(0));
        this.totalNumberAddedLabel.setName(TOTAL_NUMBER_LABEL_NAME);

        this.gridPanel = new JPanel(new GridLayout(0, 25));
        this.gridPanel.setName(GRID_PANEL_NAME);

        this.selectFileButton = new JButton("Select file");
        this.selectFileButton.setName(SELECT_FILE_BUTTON_NAME);

        this.selectNumbersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.selectNumbersPanel.setName(SELECT_NUMBER_PANEL_NAME);

        this.progressMonitor = new ProgressMonitor(this,
                "Load data from file", "", 0, 100);
        this.progressMonitor.setMillisToDecideToPopup(500);
        this.includeNumbers = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

        setLayout(new BorderLayout());
        add(createSelectFileAndFilterPanel(), BorderLayout.NORTH);
        add(new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        tryLoadDefault();
    }

    private @NotNull JComponent createSelectFileAndFilterPanel() {
        selectFileButton.addActionListener(e -> {
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
        allCheckbox.setName(ALL_CHECKBOX_NAME);

        JCheckBox noneCheckBox = new JCheckBox("None", false);
        noneCheckBox.setName(NONE_CHECKBOX_NAME);

        allCheckbox.addActionListener(e -> {
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

        noneCheckBox.addActionListener(e -> {
            if (noneCheckBox.isSelected()) {
                includeNumbers.clear();

                for (int i = 0; i < selectNumbersPanel.getComponentCount(); i++) {
                    Component component = selectNumbersPanel.getComponent(i);
                    if (component instanceof JCheckBox checkBox) {
                        checkBox.setSelected(false);
                    }
                }

                noneCheckBox.setSelected(true);
                totalNumberAddedLabel.setText(TOTAL_NUMBER_TEXT.formatted(0));
                gridPanel.removeAll();
                gridPanel.repaint();
            }
        });

        for (int i = 0; i < 10; i++) {
            JCheckBox checkBox = new JCheckBox(String.valueOf(i), true);
            checkBox.setName(MnistCsvViewer.CHECK_BOX_NUMBER_NAME + i);

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
        selectNumbersPanel.add(totalNumberAddedLabel);
    }

    private void tryLoadDefault() {
        URL defaultUrl = MnistCsvViewer.class.getResource("/mnist/" + DEFAULT_FILE_NAME);

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
            totalNumberAddedLabel.setText(TOTAL_NUMBER_TEXT.formatted(0));
            return;
        }

        try {
            long fileSize = Files.size(currentPath);
            if (fileSize > 1024 * 1024) {
                selectFileButton.setEnabled(false);
                setPanelEnabled(selectNumbersPanel, false);
            }

            createLoadTask().execute();
        } catch (IOException e) {
            showErrorImportDialog();
        }
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

    private void onLoadCancel() {
        currentPath = null;
        fileNameLabel.setText("no file selected");
        totalNumberAddedLabel.setText(TOTAL_NUMBER_TEXT.formatted(0));
        gridPanel.removeAll();
    }

    private void showScaledViewDialog(BufferedImage image, String number) {
        int scale = 10;
        int originalSize = image.getHeight();
        int scaleSize = originalSize * scale;
        BufferedImage outputImage = new BufferedImage(scaleSize, scaleSize, BufferedImage.TYPE_BYTE_GRAY);
        outputImage.getGraphics().drawImage(image.getScaledInstance(scaleSize, scaleSize, Image.SCALE_DEFAULT),
                0, 0, null);

        final JDialog dialog = new JDialog(MnistCsvViewer.this.frame, "Number %s".formatted(number), true);
        dialog.setName(DETAIL_VIEW_DIALOG_NAME);
        dialog.getContentPane().add(new JLabel(new ImageIcon(outputImage)));
        dialog.pack();
        dialog.setLocationRelativeTo(MnistCsvViewer.this.frame);
        dialog.setVisible(true);
    }

    private class LoadFileTask extends SwingWorker<Void, DataPair> {
        private int totalNumberAdded = 0;

        @Override
        public Void doInBackground() {
            setProgress(0);

            String line;
            try (BufferedReader reader = Files.newBufferedReader(currentPath)) {
                long fileSize = Files.size(currentPath);
                if (fileSize == 0) {
                    throw new IllegalArgumentException("File is empty");
                }

                long readTotal = 0;
                long chunk = 0;
                while ((line = reader.readLine()) != null && !isCancelled()) {
                    chunk += line.getBytes(StandardCharsets.UTF_8).length;
                    if (chunk > fileSize / 100) {
                        readTotal += chunk;
                        chunk = 0;
                        setProgress((int) ((readTotal / (double) fileSize) * 100));
                    }

                    String[] split = line.split(",");

                    if ((split.length == 0) || (split.length - 1) % 2 != 0) {
                        throw new IllegalArgumentException("Wrong CSV file");
                    }

                    int number = Integer.parseInt(split[0]);
                    if (!includeNumbers.contains(number)) {
                        continue;
                    }

                    int size = (int) Math.pow((split.length - 1), 0.5);
                    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
                    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
                    for (int i = 1; i < split.length; i++) {
                        dataBuffer.setElem(i - 1, 255 - Integer.parseInt(split[i]));
                    }

                    publish(new DataPair(image, String.valueOf(number)));
                    totalNumberAdded++;
                }
            } catch (Exception e) {
                currentPath = null;
                SwingUtilities.invokeLater(MnistCsvViewer.this::showErrorImportDialog);
            }

            setProgress(100);
            return null;
        }

        @Override
        protected void process(List<DataPair> chunks) {
            for (DataPair pair : chunks) {
                gridPanel.add(createNumberLabel(pair.image, pair.number));
            }
        }

        @Override
        public void done() {
            if (currentPath == null || isCancelled()) {
                onLoadCancel();
            } else {
                totalNumberAddedLabel.setText(TOTAL_NUMBER_TEXT.formatted(totalNumberAdded));
            }

            setPanelEnabled(selectNumbersPanel, true);
            selectFileButton.setEnabled(true);
            gridPanel.repaint();
            frame.validate();
        }

        private @NotNull JLabel createNumberLabel(@NotNull BufferedImage image,
                                                  @NotNull String number) {
            JLabel jLabel = new JLabel(number);
            jLabel.setIcon(new ImageIcon(image));
            jLabel.setHorizontalTextPosition(JLabel.CENTER);
            jLabel.setVerticalTextPosition(JLabel.BOTTOM);
            jLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showScaledViewDialog(image, number);
                }
            });

            return jLabel;
        }
    }

    private void showErrorImportDialog() {
        JOptionPane.showMessageDialog(frame, "Wrong CSV Fils", IMPORT_ERROR_TEXT,
                JOptionPane.ERROR_MESSAGE);
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

    private record DataPair(@NotNull BufferedImage image, @NotNull String number) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MnistCsvViewer::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame(FRAME_TITLE);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 350);
        f.getContentPane().add(new MnistCsvViewer(f));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    //==== UI Component names =====
    public final static String FRAME_TITLE = "Mnist Data Set Viewer";
    public final static String SELECT_FILE_BUTTON_NAME = "SelectFileButton";
    public final static String FILE_NAME_LABEL_NAME = "FileNameLabel";
    public final static String TOTAL_NUMBER_LABEL_NAME = "TotalNumberLabel";
    public final static String GRID_PANEL_NAME = "GridPanel";
    public final static String SELECT_NUMBER_PANEL_NAME = "SelectNumberPanel";
    public final static String TOTAL_NUMBER_TEXT = "Total Number: %d";
    public final static String DEFAULT_FILE_NAME = "mnist_train_100.csv";
    public final static String NONE_CHECKBOX_NAME = "NoneCheckbox";
    public final static String ALL_CHECKBOX_NAME = "AllCheckbox";
    public final static String CHECK_BOX_NUMBER_NAME = "CheckBoxNumber";
    public final static String DETAIL_VIEW_DIALOG_NAME = "DetailViewDialog";
    public final static String IMPORT_ERROR_TEXT = "Import Error";
}
