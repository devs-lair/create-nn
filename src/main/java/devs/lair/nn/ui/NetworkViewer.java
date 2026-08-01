package devs.lair.nn.ui;

import com.formdev.flatlaf.*;
import devs.lair.nn.MatrixUtils;
import devs.lair.nn.NetworkStorage;
import devs.lair.nn.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class NetworkViewer extends JPanel {
    private final JFrame frame;
    private final JPanel infoPanel;
    private final JTable inputToHiddenTable;
    private final JTable hiddenToOutputTable;

    private JFileChooser fc;
    private NeuralNetwork nn;
    private Path path;

    public NetworkViewer(@NotNull JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        this.infoPanel = new JPanel(new GridLayout(5, 2));
        this.infoPanel.setSize(new Dimension(200, 200));
        this.inputToHiddenTable = new JTable();
        this.hiddenToOutputTable = new JTable();

        createMenu();
        setBorder(BorderFactory.
                createEmptyBorder(5, 5, 5, 5));

        add(new JLabel("Open Neural Network", SwingConstants.CENTER),
                BorderLayout.CENTER);

        onFileSelect(Paths.get("nn-test.csv"));
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        menuBar.add(file);

        JMenuItem openNetwork = new JMenuItem("Open Network");
        JMenuItem close = new JMenuItem("Close");

        openNetwork.addActionListener(e -> openFileChooser());
        close.addActionListener(e -> frame.dispose());

        file.add(openNetwork);
        file.add(close);

        frame.setJMenuBar(menuBar);
    }

    private void openFileChooser() {
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
    }

    public void onFileSelect(@NotNull Path path) {
        this.path = path;

        try {
            nn = NetworkStorage.loadFromFile(path);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Wrong CSV File", IMPORT_ERROR_TEXT,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        removeAll();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Info", createInfoPanel());
        tabs.addTab("Query", createQueryPanel());
        tabs.addTab("Back query", new JLabel("Test"));

        this.add(tabs, BorderLayout.CENTER);

        validate();
        repaint();
    }

    private JComponent createQueryPanel() {
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));

        queryPanel.add(new JLabel("Draw number here:"));

        JPanel flowPanel = new JPanel(new FlowLayout());
        DrawingPanel dp = new DrawingPanel();
        flowPanel.add(dp);

        queryPanel.add(flowPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            dp.clear();
        });

        JTextArea answer = new JTextArea();
        answer.setLineWrap(true);
        JButton queryButton = new JButton("Query");
        queryButton.addActionListener(e -> onQuery(dp, answer));

        buttonsPanel.add(clearButton);
        buttonsPanel.add(queryButton);
        queryPanel.add(buttonsPanel);
        queryPanel.add(answer);

        return queryPanel;
    }

    public static BufferedImage blur(BufferedImage img) {
        float val = 1.0f / 100.0f;
        float[] filter = new float[100];
        Arrays.fill(filter, val);

        BufferedImageOp op = new ConvolveOp(new Kernel(10, 10, filter ));
        return op.filter(img, null);
    }

    private void onQuery(DrawingPanel dp, JTextArea answerLabel) {
        BufferedImage image = blur(dp.getImage());

        BufferedImage outputImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        outputImage.getGraphics().drawImage(image.getScaledInstance(28, 28, Image.SCALE_DEFAULT), 0, 0, null);

        int[] query = new int[28 * 28];
        DataBuffer dataBuffer = outputImage.getRaster().getDataBuffer();

        for (int i = 0; i < query.length; i++) {
            query[i] = 255 - dataBuffer.getElem(i);
        }

        double[] input = normalizeQuery(query);
        double[][] answer = nn.query(input);
        answerLabel.setText(getIndexOfMaxElementInOutputs(answer) + " - " + MatrixUtils.toString(answer));
        dp.clear();
    }

    private double[] normalizeQuery(int[] query) {
        double[] result = new double[query.length];
        for (int i = 0; i < query.length; i++) {
            result[i] = (query[i] / (double) 255) * 0.97 + 0.01;
        }
        return result;
    }

    private static int getIndexOfMaxElementInOutputs(double[][] output) {
        double max = Double.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < output.length; i++) {
            double current = output[i][0];
            if (current > max) {
                max = current;
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    private JComponent createInfoPanel() {
        JPanel infoTab = new JPanel();
        infoTab.setLayout(new BoxLayout(infoTab, BoxLayout.Y_AXIS));

        infoTab.add(infoPanel, BorderLayout.NORTH);

        infoTab.add(new JLabel("Intput to hidden weights:", SwingConstants.LEFT), BorderLayout.NORTH);
        infoTab.add(new JScrollPane(inputToHiddenTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        infoTab.add(new JLabel("Hidden to output weights:", JLabel.LEFT), BorderLayout.NORTH);
        infoTab.add(new JScrollPane(hiddenToOutputTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        setNetworkToInfoPanel();
        setNetworkToWeightTables(nn.getInputToHiddenWeights(), inputToHiddenTable);
        setNetworkToWeightTables(nn.getHiddenToOutputsWeights(), hiddenToOutputTable);

        return infoTab;
    }

    private void setNetworkToWeightTables(double[][] weights, @NotNull JTable table) {
        Object[][] data = new Object[weights.length][weights[0].length];
        Object[] names = new Object[weights[0].length];

        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                data[i][j] = weights[i][j];
                if (i == 0) {
                    names[j] = j + 1;
                }
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, names) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(model);
        table.setShowGrid(true);

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(55);
        }
    }

    private void setNetworkToInfoPanel() {
        infoPanel.removeAll();

        infoPanel.add(new JLabel("Input file"));
        infoPanel.add(new JLabel(String.valueOf(nn.getInputNodesNumber())));

        infoPanel.add(new JLabel("Number of inputs nodes"));
        infoPanel.add(new JLabel(String.valueOf(nn.getInputNodesNumber())));

        infoPanel.add(new JLabel("Number of hidden nodes"));
        infoPanel.add(new JLabel(String.valueOf(nn.getHiddenNodesNumber())));

        infoPanel.add(new JLabel("Number of outputs nodes"));
        infoPanel.add(new JLabel(String.valueOf(nn.getOutputNodesNumber())));

        infoPanel.add(new JLabel("Learning rate"));
        infoPanel.add(new JLabel(String.valueOf(nn.getLearningRate())));

        infoPanel.validate();
        infoPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NetworkViewer::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        FlatIntelliJLaf.setup();

        JFrame f = new JFrame(FRAME_TITLE);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 800);
        f.getContentPane().add(new NetworkViewer(f));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public final static String FRAME_TITLE = "Network Viewer";
    public final static String IMPORT_ERROR_TEXT = "Import Error";
}
