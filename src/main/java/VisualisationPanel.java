import javax.swing.*;
import java.awt.*;

public class VisualisationPanel extends JPanel {

    private final DisplayBuffer displayBuffer;

    public VisualisationPanel(DisplayBuffer displayBuffer) {
        this.displayBuffer = displayBuffer;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Rectangle clipBounds = g.getClipBounds();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, (int) clipBounds.getWidth(), (int) clipBounds.getHeight());

        if (displayBuffer == null)
            return;

        double dx = clipBounds.getWidth() / displayBuffer.getBufferSize();

        drawData(g, clipBounds, dx, Color.BLACK, displayBuffer.getRmsBuffer(), 1d);
        drawData(g, clipBounds, dx, Color.RED, displayBuffer.getMidiBuffer(), 127d);
        drawData(g, clipBounds, dx, Color.GREEN, displayBuffer.getMidiBuffer(), 127d);

    }

    private void drawData(Graphics g, Rectangle clipBounds, double dx, Color color, Iterable<? extends Number> data, double factor) {
        g.setColor(color);
        double x = 0;
        double y = 0;
        for (Number sample : data) {
            double x2 = x + dx;
            double y2 = clipBounds.height - (sample.doubleValue()/factor * clipBounds.height);
            g.drawLine((int) x, (int)y, (int)x2, (int)y2);
            x = x2;
            y = y2;
        }
    }

}
