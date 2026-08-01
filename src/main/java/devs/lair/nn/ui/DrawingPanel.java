package devs.lair.nn.ui;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel {
    private List<Point> points;
    private int pointCounter = 0;

    public DrawingPanel() {

        points = new ArrayList<>();
        setSize(280, 280);
        setPreferredSize(new Dimension(280, 280));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                points.add(event.getPoint());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                points.add(event.getPoint());
                repaint();
            }
        });
    }

    public void clear() {
        points.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        float radius = 12;
        float[] dist = {0.1f, 0.9f};
        Color[] colors = { Color.BLACK,  Color.WHITE};

        Graphics2D g2d = (Graphics2D) g;


        for (Point point : points) {
//            RadialGradientPaint brush =
//                    new RadialGradientPaint(point, radius, dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
//            g2d.setPaint(brush);
            g.fillOval(point.x-10, point.y-10, 40, 40);
        }
    }

    public List<Point> getPoints() {
        return points;
    }

    public BufferedImage getImage() {


        BufferedImage bi = new BufferedImage(280, 280, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = bi.createGraphics();
        this.print(g);
        g.dispose();
        return bi;
    }
}