import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class UserAudioOption
{
    private final Mixer mixer;
    private final Line.Info targetLine;
    private final String name;

    public UserAudioOption(Mixer mixer, Line.Info targetLine, String name) {
        this.mixer = mixer;
        this.targetLine = targetLine;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public TargetDataLine getLine() {
        try {
            mixer.open();
            return (TargetDataLine) mixer.getLine(targetLine);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
