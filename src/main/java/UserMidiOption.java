import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class UserMidiOption {
    private final MidiDevice.Info info;

    public UserMidiOption(MidiDevice.Info info) {
        this.info = info;
    }

    public String toString()
    {
        return info.getName() + " " + info.getDescription();
    }

    public MidiDevice getDevice() {
        try {
            return MidiSystem.getMidiDevice(info);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
