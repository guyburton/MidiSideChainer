import java.util.concurrent.LinkedBlockingDeque;

public class Filter {

    private LinkedBlockingDeque<Byte> linkedBlockingDeque = new LinkedBlockingDeque<>();

    private int length = 1;

    public void setLength(int length)
    {
        this.length = length;
    }

    public byte getNextValue(byte sample) {
        double outputSample = getOutputSample(sample);
        return (byte)outputSample;
    }

    private double getOutputSample(byte inputSample) {
        linkedBlockingDeque.add(inputSample);
        while (linkedBlockingDeque.size() > length)
            linkedBlockingDeque.removeFirst();

        return linkedBlockingDeque.stream().mapToDouble(o -> o).sum() / linkedBlockingDeque.size();
    }
}
