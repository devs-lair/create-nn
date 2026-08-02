package devs.lair.nn.ui.networkviewer.tabs;

import devs.lair.nn.NeuralNetwork;
import devs.lair.nn.ui.networkviewer.DrawingPanel;
import devs.lair.nn.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.*;

public class QueryTab extends JPanel {
    private final NeuralNetwork nn;

    private final JButton correctButton;
    private final JButton incorrectButton;
    private final JTable answerTable;
    private final DrawingPanel dp;

    private int totalQuery = 0;
    private int correct = 0;

    public QueryTab(@NotNull NeuralNetwork nn) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.nn = nn;

        this.correctButton = new JButton("Correct");
        this.incorrectButton = new JButton("Incorrect");
        this.correctButton.setName(CORRECT_BUTTON_NAME);
        this.incorrectButton.setName(INCORRECT_BUTTON_NAME);
        this.dp = new DrawingPanel();

        this.correctButton.addActionListener(e -> {
            totalQuery++;
            correct++;

            onValidate();
        });

        this.incorrectButton.addActionListener(e -> {
            totalQuery++;

            onValidate();
        });

        this.answerTable = new JTable(new DefaultTableModel(
                new Object[]{"Answer", "Output"}, 12));

        add(createDrawingPanel());
        add(Box.createHorizontalStrut(10));
        add(createAnswerPanel());
    }

    private void onValidate() {
        correctButton.setEnabled(false);
        incorrectButton.setEnabled(false);
        dp.clear();

        setPerformance();
    }

    private JComponent createAnswerPanel() {
        JPanel answerPanel = new JPanel(new BorderLayout());
        answerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        answerPanel.setPreferredSize(new Dimension(205, 350));
        answerPanel.setMaximumSize(new Dimension(205, 350));
        answerPanel.setMinimumSize(new Dimension(205, 350));
        answerPanel.setAlignmentY(TOP_ALIGNMENT);

        answerTable.setAlignmentY(TOP_ALIGNMENT);
        answerTable.setShowGrid(true);
        answerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < answerTable.getColumnModel().getColumnCount(); i++) {
            answerTable.getColumnModel().getColumn(i).setPreferredWidth(100);
        }

        answerPanel.add(answerTable.getTableHeader(), BorderLayout.NORTH);
        answerPanel.add(answerTable, BorderLayout.CENTER);

        JPanel controls = createAnswerControls();
        answerPanel.add(controls, BorderLayout.SOUTH);

        return answerPanel;
    }

    private @NotNull JPanel createAnswerControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        controls.add(correctButton);
        controls.add(incorrectButton);
        return controls;
    }

    private void setPerformance() {
        answerTable.getModel().setValueAt(correct / (double) totalQuery, 11, 1);

    }

    private @NotNull JPanel createDrawingPanel() {
        JPanel drawing = new JPanel();
        drawing.setPreferredSize(new Dimension(280, 400));
        drawing.setMaximumSize(new Dimension(280, 400));
        drawing.setMinimumSize(new Dimension(280, 400));
        drawing.setLayout(new BoxLayout(drawing, BoxLayout.Y_AXIS));
        drawing.setAlignmentY(TOP_ALIGNMENT);

        JLabel drawHereLabel = new JLabel("Draw number here:");
        drawHereLabel.setAlignmentX(LEFT_ALIGNMENT);
        drawing.add(drawHereLabel);
        dp.setAlignmentX(LEFT_ALIGNMENT);
        drawing.add(dp);

        JPanel controls = createDrawPanelControls(dp);
        drawing.add(controls);
        return drawing;
    }

    private @NotNull JPanel createDrawPanelControls(@NotNull DrawingPanel dp) {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        controls.setBorder(null);
        controls.setAlignmentX(LEFT_ALIGNMENT);

        JButton brushPlus = new JButton("+");
        JButton brushMinus = new JButton("-");
        JButton clearButton = new JButton("Clear");
        JButton queryButton = new JButton("Query");

        brushPlus.setName(BRUSH_PLUS_BUTTON_NAME);
        brushMinus.setName(BRUSH_MINUS_BUTTON_NAME);
        clearButton.setName(CLEAR_BUTTON_NAME);
        queryButton.setName(QUERY_BUTTON_NAME);

        brushPlus.addActionListener(e -> dp.brushPlus());
        brushMinus.addActionListener(e -> dp.brushMinus());

        clearButton.addActionListener(e -> dp.clear());
        queryButton.addActionListener(e -> onQuery(dp));

        controls.add(brushPlus);
        controls.add(brushMinus);
        controls.add(clearButton);
        controls.add(queryButton);
        return controls;
    }

    private void onQuery(@NotNull DrawingPanel dp) {
        BufferedImage image = ImageUtils.blur(dp.getImage());
        BufferedImage scaledImage = ImageUtils.scale(image, 0.1f);

        int[] query = new int[scaledImage.getWidth() * scaledImage.getHeight()];
        DataBuffer dataBuffer = scaledImage.getRaster().getDataBuffer();

        for (int i = 0; i < query.length; i++) {
            query[i] = 255 - dataBuffer.getElem(i);
        }

        double[] input = normalizeQuery(query);
        double[][] answer = nn.query(input);

        setAnswerToPanel(answer);
        dp.shade();

        correctButton.setEnabled(true);
        incorrectButton.setEnabled(true);
    }

    private void setAnswerToPanel(double[][] answer) {
        int maxIndex = getIndexOfMaxElementInOutputs(answer);

        Object[][] data = new Object[12][2];
        for (int i = 0; i < 12; i++) {
            if (i == 10) {
                data[i][0] = null;
                data[i][1] = null;
            } else if (i == 11) {
                data[i][0] = "Performance";
                data[i][1] = totalQuery == 0 ? 0 : correct / (double) totalQuery;
            } else {
                data[i][0] = i == maxIndex ? String.valueOf(i) : null;
                data[i][1] = answer[i][0];
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, new Object[]{"Answer", "Output"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return String.class;
                }

                if (columnIndex == 1) {
                    return Double.class;
                }

                return Object.class;
            }
        };

        answerTable.setModel(model);
    }

    private double[] normalizeQuery(int[] query) {
        double[] result = new double[query.length];
        for (int i = 0; i < query.length; i++) {
            result[i] = (query[i] / (double) 255) * 0.99 + 0.01;
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

    public final static String QUERY_BUTTON_NAME = "QueryButton";
    public final static String CLEAR_BUTTON_NAME = "ClearButton";
    public final static String BRUSH_PLUS_BUTTON_NAME = "BrushPlusButton";
    public final static String BRUSH_MINUS_BUTTON_NAME = "BrushMinusButton";
    public final static String CORRECT_BUTTON_NAME = "CorrectButton";
    public final static String INCORRECT_BUTTON_NAME = "IncorrectButton";
}
