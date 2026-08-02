package devs.lair.nn.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;

public class ImageUtils {

    private ImageUtils() {
        throw new UnsupportedOperationException();
    }

    public static BufferedImage createFromCsvSplit(@NotNull String[] split) {

        int size = (int) Math.pow((split.length - 1), 0.5);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        DataBuffer dataBuffer = image.getRaster().getDataBuffer();
        for (int i = 1; i < split.length; i++) {
            dataBuffer.setElem(i - 1, 255 - Integer.parseInt(split[i]));
        }

        return image;
    }

    public static BufferedImage scale(@NotNull BufferedImage originalImage, float scale) {
        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);

        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_BYTE_GRAY);
        scaledImage.getGraphics().drawImage(originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT),
                0, 0, null);

        return scaledImage;
    }

    public static BufferedImage blur(@NotNull BufferedImage img) {
        float val = 1.0f / 100.0f;
        float[] filter = new float[100];
        Arrays.fill(filter, val);

        BufferedImageOp op = new ConvolveOp(new Kernel(10, 10, filter));
        return op.filter(img, null);
    }
}
