package devs.lair.nn.ui.networkviewer;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel {
    public static final String DRAWING_PANEL_NAME = "DrawingPanelName";
    private final List<PointAndBrush> points;
    private int brushSize = 30;
    private boolean shade;

    public DrawingPanel() {

        setName(DRAWING_PANEL_NAME);
        points = new ArrayList<>();
        setSize(280, 280);
        setPreferredSize(new Dimension(280, 280));
        setMaximumSize(new Dimension(280, 280));
        setMinimumSize(new Dimension(280, 280));

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (shade) {
                    points.clear();
                    shade = false;
                }
                points.add(new PointAndBrush(event.getPoint(), brushSize));
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (shade) {
                    points.clear();
                    shade = false;
                }

                points.add(new PointAndBrush(event.getPoint(), brushSize));
                repaint();
            }
        });

        setCustomCursor();
    }

    private void setCustomCursor() {
        BufferedImage bufferedImage = new BufferedImage(brushSize, brushSize, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.drawOval(0, 0, brushSize-1, brushSize-1);
        graphics.dispose();

        Point hotSpot = new Point(brushSize/2, brushSize/2);
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(bufferedImage, hotSpot, "My Custom Cursor");
        setCursor(customCursor);
    }

    public void clear() {
        points.clear();
        repaint();
    }

    public void shade() {
        this.shade = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (PointAndBrush pointAndBrush : points) {
            Point point = pointAndBrush.point;
            int brush = pointAndBrush.brushSize;

            if (shade) {
                g.setColor(Color.LIGHT_GRAY);
            }

            g.fillOval(point.x - brush / 2, point.y - brush / 2, brush, brush);
        }
    }

    public BufferedImage getImage() {
        BufferedImage bi = new BufferedImage(280, 280, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = bi.createGraphics();
        this.print(g);
        g.dispose();
        return bi;
    }

    public void brushPlus() {
        brushSize += 2;
        setCustomCursor();
    }

    public void brushMinus() {
        if (brushSize == 2) {
            return;
        }

        brushSize -= 2;
        setCustomCursor();
    }

    private record PointAndBrush(@NotNull Point point, int brushSize){

    }
}