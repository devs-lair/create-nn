package devs.lair.nn.ui.networkviewer.tabs;

import devs.lair.nn.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;

public class InfoTab extends JPanel {
    private final NeuralNetwork nn;
    private final Path path;

    public InfoTab(@NotNull NeuralNetwork nn, @NotNull Path path) {
        this.nn = nn;
        this.path = path;

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        JTable inputToHiddenTable = new JTable();
        JTable hiddenToOutputTable = new JTable();

        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 0));
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));

        flowPanel.setAlignmentX(LEFT_ALIGNMENT);
        flowPanel.setBorder(BorderFactory.createEmptyBorder());
        flowPanel.add(infoPanel);

        add(flowPanel, BorderLayout.NORTH);
        add(Box.createVerticalStrut(10));

        JLabel labelInput = new JLabel("Intput to hidden weights:");
        labelInput.setAlignmentX(LEFT_ALIGNMENT);
        add(labelInput);

        JScrollPane scrollPaneInput = new JScrollPane(inputToHiddenTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneInput.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPaneInput);

        JLabel labelHidden = new JLabel("Hidden to output weights:");
        labelHidden.setAlignmentX(LEFT_ALIGNMENT);
        add(labelHidden);

        JScrollPane hiddenScrollPane = new JScrollPane(hiddenToOutputTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        hiddenScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(hiddenScrollPane);

        //fil ui
        setNetworkToInfoPanel(infoPanel);
        setNetworkToWeightTables(nn.getInputToHiddenWeights(), inputToHiddenTable);
        setNetworkToWeightTables(nn.getHiddenToOutputsWeights(), hiddenToOutputTable);
    }

    private void setNetworkToInfoPanel(@NotNull JPanel infoPanel) {
        infoPanel.add(new JLabel("Input file"));
        infoPanel.add(new JLabel(String.valueOf(path.getFileName())));

        infoPanel.add(new JLabel("Number of inputs nodes"));
        infoPanel.add(new JLabel(String.valueOf(nn.getInputNodesNumber())));

        infoPanel.add(new JLabel("Number of hidden nodes"));
        infoPanel.add(new JLabel(String.valueOf(nn.getHiddenNodesNumber())));

        infoPanel.add(new JLabel("Number of outputs nodes"));
        infoPanel.add(new JLabel(String.valueOf(nn.getOutputNodesNumber())));

        infoPanel.add(new JLabel("Learning rate"));
        infoPanel.add(new JLabel(String.valueOf(nn.getLearningRate())));
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
        table.setShowGrid(true);
        table.setModel(model);

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(55);
        }
    }
}
