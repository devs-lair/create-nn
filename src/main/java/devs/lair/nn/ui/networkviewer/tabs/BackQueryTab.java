package devs.lair.nn.ui.networkviewer.tabs;

import devs.lair.nn.NeuralNetwork;
import devs.lair.nn.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Arrays;

public class BackQueryTab extends JPanel {
    public BackQueryTab(@NotNull NeuralNetwork nn) {
        super(new GridLayout(2, 5));

        for (int i = 0; i < 10; i++) {
            double[] target = new double[10];
            Arrays.fill(target, 0.01);
            target[i] = 0.99;

            double[][] inputs = nn.backQuery(target);

            BufferedImage outputImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
            DataBuffer dataBuffer = outputImage.getRaster().getDataBuffer();

            for (int k = 0; k < inputs.length; k++) {
                dataBuffer.setElem(k, (int) (255 * inputs[k][0]));
            }

            BufferedImage scaledImage = ImageUtils.scale(outputImage, 5f);

            JLabel jLabel = new JLabel(new ImageIcon(scaledImage));
            jLabel.setText(String.valueOf(i));
            jLabel.setHorizontalTextPosition(JLabel.CENTER);
            jLabel.setVerticalTextPosition(JLabel.BOTTOM);
            jLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            add(jLabel);
        }
    }
}
