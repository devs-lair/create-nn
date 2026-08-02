package devs.lair.nn.ui.networkviewer;

import com.formdev.flatlaf.*;
import devs.lair.nn.NetworkStorage;
import devs.lair.nn.NeuralNetwork;
import devs.lair.nn.ui.CsvFilter;
import devs.lair.nn.ui.networkviewer.tabs.BackQueryTab;
import devs.lair.nn.ui.networkviewer.tabs.InfoTab;
import devs.lair.nn.ui.networkviewer.tabs.QueryTab;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetworkViewer extends JFrame {

    public static final String OPEN_NETWORK_MENU_TITLE = "Open Network...";
    public static final String CLOSE_MENU_TITLE = "Close";
    private final JLabel welcomeLabel;
    private final JTabbedPane tabs;
    private JFileChooser fc;

    public NetworkViewer() {
        super(FRAME_TITLE);

        this.tabs = new JTabbedPane();
        this.tabs.setName(TABS_NAME);

        createMenu();
        add(welcomeLabel = new JLabel("Open Neural Network", SwingConstants.CENTER));
        onFileSelect(Paths.get("nn-test.csv"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        menuBar.add(file);

        file.add(new JMenuItem(new AbstractAction(OPEN_NETWORK_MENU_TITLE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        }));

        file.add(new JMenuItem(new AbstractAction(CLOSE_MENU_TITLE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }));

        setJMenuBar(menuBar);
    }

    private void openFileChooser() {
        if (fc == null) {
            fc = new JFileChooser();
        }

        fc.addChoosableFileFilter(new CsvFilter());
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            onFileSelect(Paths.get(fc.getSelectedFile().getPath()));
        }

        fc.setSelectedFile(null);
    }

    public void onFileSelect(@NotNull Path path) {
        NeuralNetwork nn;
        try {
            nn = NetworkStorage.loadFromFile(path);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Wrong CSV File", IMPORT_ERROR_TEXT,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        remove(welcomeLabel);
        remove(tabs);

        tabs.removeAll();
        tabs.addTab(INFO_TAB_TITLE, new InfoTab(nn, path));
        tabs.addTab(QUERY_TAB_TITLE, new QueryTab(nn));
        tabs.addTab(BACK_QUERY_TAB_TITLE, new BackQueryTab(nn));
        tabs.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
        add(tabs, BorderLayout.CENTER);

        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatIntelliJLaf.setup();
            new NetworkViewer();
        });
    }

    public final static String FRAME_TITLE = "Network Viewer";
    public final static String IMPORT_ERROR_TEXT = "Import Error";
    public static final String TABS_NAME = "TabsName";
    public static final String INFO_TAB_TITLE = "Info";
    public static final String QUERY_TAB_TITLE = "Query";
    public static final String BACK_QUERY_TAB_TITLE = "Back query";
}
