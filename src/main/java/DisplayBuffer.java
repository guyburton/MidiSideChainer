import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DisplayBuffer {

    public static final int BUFFER_SIZE = 100;
    private Queue<Double> previousSamples = new ConcurrentLinkedQueue<>();
    private Queue<Byte> previousMidiValues = new ConcurrentLinkedQueue<>();

    public int getBufferSize() {
        return BUFFER_SIZE;
    }

    public Collection<Byte> getMidiBuffer() {
        return previousMidiValues;
    }

    public Collection<Double> getRmsBuffer() {
        return previousSamples;
    }

    public void addSample(double rmsValue) {
        previousSamples.add(rmsValue);
        if (previousSamples.size() > BUFFER_SIZE)
            previousSamples.remove();
    }

    public void addMidiSample(byte midiValue) {
        previousMidiValues.add(midiValue);
        if (previousMidiValues.size() > 100)
            previousMidiValues.remove();
    }
}
