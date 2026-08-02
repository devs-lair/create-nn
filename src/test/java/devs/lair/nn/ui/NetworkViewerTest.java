package devs.lair.nn.ui;

import devs.lair.nn.ui.networkviewer.NetworkViewer;
import devs.lair.nn.ui.networkviewer.tabs.BackQueryTab;
import devs.lair.nn.ui.networkviewer.tabs.QueryTab;
import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.data.Index;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.DialogMatcher.withTitle;
import static org.assertj.swing.edt.GuiActionRunner.execute;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("ui")
//@Disabled("Local only")

public class NetworkViewerTest extends AssertJSwingTestCaseTemplate {
    protected FrameFixture frame;

    @BeforeAll
    public void setUpOnce() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
                "Automated UI Test cannot be executed in headless environment");
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    public void startApp() {
        setUpRobot();
        frame = new FrameFixture(robot(), Objects.requireNonNull(
                execute(NetworkViewer::new)));
        frame.show();

        robot().settings().delayBetweenEvents(100);
        robot().settings().idleTimeout(100);
    }

    @AfterEach
    public void tearDown() {
        frame.cleanUp();
        frame = null;
        cleanUp();
    }

    @Test
    @DisplayName("UI Exist")
    void existUI() {
        assertThat(frame).isNotNull();

        JTabbedPaneFixture tabs = frame.tabbedPane(NetworkViewer.TABS_NAME);
        tabs.requireSelectedTab(Index.atIndex(0));
        tabs.requireTabTitles(NetworkViewer.INFO_TAB_TITLE, NetworkViewer.QUERY_TAB_TITLE,
                NetworkViewer.BACK_QUERY_TAB_TITLE);
    }

    @Test
    @DisplayName("Wrong csv file")
    void selectWrongFile() {
        JMenuItemFixture openMenu = frame.menuItemWithPath("File", NetworkViewer.OPEN_NETWORK_MENU_TITLE);
        openMenu.click();

        URL defaultUrl = MnistCsvViewer.class.getResource("/wrong.csv");
        assertThat(defaultUrl).isNotNull();
        frame.fileChooser().selectFile(Paths.get(defaultUrl.getFile()).toFile());
        frame.fileChooser().approve();
        frame.dialog(withTitle(MnistCsvViewer.IMPORT_ERROR_TEXT)).close();
    }

    @Test
    @DisplayName("Query tab")
    void queryTabTest() {
        JTabbedPaneFixture tabs = frame.tabbedPane();
        tabs.selectTab(1);

        frame.button(QueryTab.BRUSH_PLUS_BUTTON_NAME).click();
        frame.button(QueryTab.BRUSH_MINUS_BUTTON_NAME).click();
        frame.button(QueryTab.CLEAR_BUTTON_NAME).click();

        JButtonFixture queryButton = frame.button(QueryTab.QUERY_BUTTON_NAME);

        queryButton.click();
        JButtonFixture correctButton = frame.button(QueryTab.CORRECT_BUTTON_NAME);
        JButtonFixture incorrectButton = frame.button(QueryTab.INCORRECT_BUTTON_NAME);

        correctButton.click();
        incorrectButton.requireDisabled();
        correctButton.requireDisabled();

        queryButton.click();
        incorrectButton.requireEnabled();
        correctButton.requireEnabled();

        incorrectButton.click();
        incorrectButton.requireDisabled();
        correctButton.requireDisabled();
    }

    @Test
    @DisplayName("Back query tab")
    void backQueryTab() {
        JTabbedPaneFixture tabs = frame.tabbedPane();
        tabs.selectTab(2);

        ComponentFinder finder = robot().finder();
        JPanel gripPanel = finder.find(new GenericTypeMatcher<>(JPanel.class) {
            protected boolean isMatching(@NotNull JPanel panel) {
                return BackQueryTab.BACK_QUERY_PANEL_NAME.equals(panel.getName());
            }
        });

        assertThat(gripPanel).isNotNull();
        assertThat(gripPanel.getComponentCount()).isEqualTo(10);
    }
}

