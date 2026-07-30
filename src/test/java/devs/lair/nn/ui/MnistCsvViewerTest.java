package devs.lair.nn.ui;

import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.finder.JFileChooserFinder;
import org.assertj.swing.fixture.*;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.assertj.swing.timing.Condition;
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
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("ui")
@Disabled("Local only")

public class MnistCsvViewerTest extends AssertJSwingTestCaseTemplate {
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
        application(MnistCsvViewer.class).start();
        frame = findFrame(new GenericTypeMatcher<>(Frame.class) {
            protected boolean isMatching(@NotNull Frame frame) {
                return MnistCsvViewer.FRAME_TITLE.equals(frame.getTitle());
            }
        }).using(robot());

        robot().settings().delayBetweenEvents(200);
        robot().settings().idleTimeout(200);
        //frame.show();
    }

    @Test
    @DisplayName("UI Exist")
    void findFrameTest() {
        assertThat(frame).isNotNull();

        frame.label(MnistCsvViewer.FILE_NAME_LABEL_NAME).requireText(MnistCsvViewer.DEFAULT_FILE_NAME);
        JLabelFixture totalNumber = frame.label(MnistCsvViewer.TOTAL_NUMBER_LABEL_NAME);

        pause(new Condition("Total number wait") {
            public boolean test() {
                return Boolean.TRUE.equals(execute(() ->
                        Objects.equals(totalNumber.text(), MnistCsvViewer.TOTAL_NUMBER_TEXT.formatted(100))));
            }

        }, timeout(500));

        frame.button(MnistCsvViewer.SELECT_FILE_BUTTON_NAME).isEnabled();

        ComponentFinder finder = robot().finder();
        JPanel gripPanel = finder.find(new GenericTypeMatcher<>(JPanel.class) {
            protected boolean isMatching(@NotNull JPanel panel) {
                return MnistCsvViewer.GRID_PANEL_NAME.equals(panel.getName());
            }
        });

        assertThat(gripPanel).isNotNull();
        assertThat(gripPanel.getComponentCount()).isEqualTo(100);

        JPanel selectNumberPanel = finder.find(new GenericTypeMatcher<>(JPanel.class) {
            protected boolean isMatching(@NotNull JPanel panel) {
                return MnistCsvViewer.SELECT_NUMBER_PANEL_NAME.equals(panel.getName());
            }
        });

        assertThat(selectNumberPanel).isNotNull();
        assertThat(selectNumberPanel.getComponentCount()).isEqualTo(13);
    }

    @Test
    @DisplayName("Click Select File Button")
    @Order(2)
    //nott stable
    void clickSelectFileButtonTest() {
        assertThat(frame).isNotNull();
        JButtonFixture selectButton = frame.button(MnistCsvViewer.SELECT_FILE_BUTTON_NAME);

        selectButton.click();
        JFileChooserFixture fileChooser = JFileChooserFinder.findFileChooser()
                .withTimeout(10000) // 10 секунд
                .using(robot());
        fileChooser.cancel();

        selectButton.click();
        URL defaultUrl = MnistCsvViewer.class.getResource("/mnist/mnist_test_10.csv");
        Assertions.assertNotNull(defaultUrl);

        fileChooser = JFileChooserFinder.findFileChooser()
                .withTimeout(10000) // 10 секунд
                .using(robot());
        fileChooser.selectFile(Paths.get(defaultUrl.getFile()).toFile());
        fileChooser.approve();

        ComponentFinder finder = robot().finder();
        JPanel gripPanel = finder.find(new GenericTypeMatcher<>(JPanel.class) {
            protected boolean isMatching(@NotNull JPanel panel) {
                return MnistCsvViewer.GRID_PANEL_NAME.equals(panel.getName());
            }
        });

        assertThat(gripPanel).isNotNull();
        assertThat(gripPanel.getComponentCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Select filter numbers")
    void selectNumbersInFilter() {
        assertThat(frame).isNotNull();

        JCheckBoxFixture noneCheckbox = frame.checkBox(MnistCsvViewer.NONE_CHECKBOX_NAME);
        JCheckBoxFixture allCheckbox = frame.checkBox(MnistCsvViewer.ALL_CHECKBOX_NAME);
        JCheckBoxFixture numberZeroCheckbox = frame.checkBox(MnistCsvViewer.CHECK_BOX_NUMBER_NAME + "0");

        noneCheckbox.click();
        pause(500);
        allCheckbox.requireNotSelected();
        numberZeroCheckbox.requireNotSelected();

        allCheckbox.click();
        pause(200);
        noneCheckbox.requireNotSelected();
        numberZeroCheckbox.requireSelected();

        numberZeroCheckbox.click();
        pause(200);
        allCheckbox.requireNotSelected();
        frame.label(MnistCsvViewer.TOTAL_NUMBER_LABEL_NAME).requireText(MnistCsvViewer
                .TOTAL_NUMBER_TEXT.formatted(87));

        noneCheckbox.click();
        numberZeroCheckbox.click();
        frame.label(MnistCsvViewer.TOTAL_NUMBER_LABEL_NAME).requireText(MnistCsvViewer
                .TOTAL_NUMBER_TEXT.formatted(13));

        numberZeroCheckbox.click();
        frame.label(MnistCsvViewer.TOTAL_NUMBER_LABEL_NAME).requireText(MnistCsvViewer
                .TOTAL_NUMBER_TEXT.formatted(0));
    }

    @Test
    @DisplayName("Show Detail View Dialog")
    @Order(1)
    void showDetailViewDilog() {
        assertThat(frame).isNotNull();

        ComponentFinder finder = robot().finder();
        JPanel gripPanel = finder.find(new GenericTypeMatcher<>(JPanel.class) {
            protected boolean isMatching(@NotNull JPanel panel) {
                return MnistCsvViewer.GRID_PANEL_NAME.equals(panel.getName());
            }
        });

        JLabelFixture numberLabel = frame.label(new GenericTypeMatcher<>(JLabel.class) {
            @Override
            protected boolean isMatching(@NotNull JLabel jLabel) {
                return gripPanel.getComponent(0) == jLabel;
            }
        });

        assertThat(numberLabel).isNotNull();
        numberLabel.click();

        DialogFixture dialog = frame.dialog(MnistCsvViewer.DETAIL_VIEW_DIALOG_NAME, timeout(500));
        dialog.close();
    }

    @Test
    @DisplayName("Wrong file test")
    @Order(3)
    void wrongFileTest() {
        assertThat(frame).isNotNull();

        JButtonFixture selectButton = frame.button(MnistCsvViewer.SELECT_FILE_BUTTON_NAME);

        selectButton.click();
        URL defaultUrl = MnistCsvViewer.class.getResource("/empty.csv");
        assertThat(defaultUrl).isNotNull();
        frame.fileChooser().selectFile(Paths.get(defaultUrl.getFile()).toFile());
        frame.fileChooser().approve();
        frame.dialog(withTitle(MnistCsvViewer.IMPORT_ERROR_TEXT)).close();

        selectButton.click();
        defaultUrl = MnistCsvViewer.class.getResource("/wrong.csv");
        assertThat(defaultUrl).isNotNull();
        frame.fileChooser().selectFile(Paths.get(defaultUrl.getFile()).toFile());
        frame.fileChooser().approve();
        frame.dialog(withTitle(MnistCsvViewer.IMPORT_ERROR_TEXT)).close();

        selectButton.click();
        defaultUrl = MnistCsvViewer.class.getResource("/wrongext");
        assertThat(defaultUrl).isNotNull();
        frame.fileChooser().selectFile(Paths.get(defaultUrl.getFile()).toFile());
        frame.fileChooser().approve();
        frame.dialog(withTitle(MnistCsvViewer.IMPORT_ERROR_TEXT)).close();
    }
    @Test
    @DisplayName("Cancel load big file")
    void cancelBigFile() {
        assertThat(frame).isNotNull();

        JButtonFixture selectButton = frame.button(MnistCsvViewer.SELECT_FILE_BUTTON_NAME);
        selectButton.click();

        URL defaultUrl = MnistCsvViewer.class.getResource("/mnist/mnist_train.csv");
        assertThat(defaultUrl).isNotNull();
        frame.fileChooser().selectFile(Paths.get(defaultUrl.getFile()).toFile());
        frame.fileChooser().approve();

        DialogFixture progressMonitor = frame.dialog(withTitle(UIManager.getString(
                "ProgressMonitor.progressText"))).requireVisible();

        progressMonitor.button().click();
    }

    @AfterEach
    public void tearDown() {
        frame.cleanUp();
        frame = null;
        cleanUp();
    }
}
