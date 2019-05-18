import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class MidiThread {

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private volatile Receiver receiver;
    private MidiDevice midiDevice;
    private Supplier<Double> getRmsValue;
    private DisplayBuffer displayBuffer;
    private final Filter filter = new Filter();
    private volatile boolean invert;
    private byte lastByte = 0;

    private ScheduledFuture<?> scheduledFuture;
    private int quantize = 1;
    private int delay = 0;

    private final ConcurrentLinkedQueue<Byte> delayLine = new ConcurrentLinkedQueue<>();

    public MidiThread(MidiDevice midiDevice, Supplier<Double> getRmsValue, DisplayBuffer displayBuffer) throws MidiUnavailableException {
        this.midiDevice = midiDevice;
        this.getRmsValue = getRmsValue;
        this.displayBuffer = displayBuffer;
        midiDevice.open();
        receiver = midiDevice.getReceiver();
        setPeriod(120);
    }

    public void setPeriod(int period)
    {
        if (scheduledFuture != null)
            scheduledFuture.cancel(false);
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::run, 1000, period, TimeUnit.MILLISECONDS);
    }

    public void close()
    {
        scheduledExecutorService.shutdown();
        receiver.close();
        receiver = null;
        midiDevice.close();
    }

    public void setInverted(boolean selected) {
        invert = selected;
    }

    private void run() {
        if (receiver == null)
            return;
        try {
            double rmsValue = getRmsValue.get();
            byte midiValue = (byte) Math.min(rmsValue * 127, 127);

            midiValue = filter.getNextValue(midiValue);

            if (invert)
                midiValue = (byte) (127 - midiValue);

            midiValue = quantize(midiValue);

            delayLine.add(midiValue);
            if (delayLine.size() < delay) {
                midiValue = lastByte;
            }

            while(delayLine.size() > delay)
                midiValue = delayLine.remove();

            displayBuffer.addSample(rmsValue);
            displayBuffer.addMidiSample(midiValue);

            // dedupe messages if possible to give the midiv reciever a rest
            if (lastByte == midiValue)
                return;
            lastByte = midiValue;

            receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 77, midiValue), -1);
            Thread.sleep(10);
            receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 78, 127 - midiValue), -1);

            System.out.println("Sent MIDI packet val " + midiValue + " on #cc" + 77);
            System.out.println("Sent MIDI packet val " + (127- midiValue) + " on #cc" + 78);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte quantize(byte midiValue) {

        return (byte) ((midiValue / quantize) * quantize);
    }

    public void setQuantize(int value) {
        this.quantize = value;
    }

    public void setDelay(int value) {
        this.delay = value;
    }

    public void setFilterLength(int value) {
        this.filter.setLength(value);
    }
}
