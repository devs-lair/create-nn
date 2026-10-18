package devs.lair.nn.ui.networkviewer;

import com.formdev.flatlaf.FlatIntelliJLaf;
import devs.lair.nn.RecognizeHuMomentsService;
import devs.lair.nn.ui.networkviewer.tabs.QueryTab;

import javax.swing.*;

public class RecognizeHuMomentsServiceViewer extends JFrame {

    public RecognizeHuMomentsServiceViewer() {
        super(FRAME_TITLE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setName(TABS_NAME);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        RecognizeHuMomentsService recognizeService = new RecognizeHuMomentsService();
        tabs.addTab(QUERY_TAB_TITLE, new QueryTab(recognizeService));
        add(tabs);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatIntelliJLaf.setup();
            new RecognizeHuMomentsServiceViewer();
        });
    }

    public final static String FRAME_TITLE = "Network Viewer";
    public static final String TABS_NAME = "TabsName";
    public static final String QUERY_TAB_TITLE = "Query";
}
