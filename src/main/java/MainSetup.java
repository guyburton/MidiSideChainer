import javax.sound.midi.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainSetup {

    private AudioSamplerThread audioSamplerThread;
    private MidiThread midiThread;
    private DisplayBuffer displayBuffer = new DisplayBuffer();

    public static void main(String[] args) {
        MainSetup mainSetup = new MainSetup();
        JFrame frame = new JFrame("Midi SideChainer");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500, 900));
        frame.add(new MainPanel(mainSetup));
        frame.setVisible(true);
    }

    public List<UserMidiOption> getMidiInterfaces() {
        List<UserMidiOption> options = new ArrayList<>();
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
                midiDevice.open();
                Receiver receiver = midiDevice.getReceiver();
                receiver.close();
                midiDevice.close();
            } catch (Exception e) {
                continue;
            }

            options.add(new UserMidiOption(info));
        }

        return options;
    }

    public void setMidiInterface(UserMidiOption option) {
        if (midiThread != null) {
            midiThread.close();
        }
        try {
            midiThread = new MidiThread(option.getDevice(), audioSamplerThread::getRms, displayBuffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setAudioInterface(UserAudioOption userAudioOption) throws InterruptedException {
        if (audioSamplerThread != null)
            audioSamplerThread.close();

        TargetDataLine targetDataLine = userAudioOption.getLine();
        if (targetDataLine != null) {
            audioSamplerThread = new AudioSamplerThread(targetDataLine);
            audioSamplerThread.start();
        }
    }

    public void setInverted(boolean selected) {
        midiThread.setInverted(selected);
    }

    public List<UserAudioOption> getAudioInterfaces() {
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        List<UserAudioOption> userAudioOptions = new ArrayList<>();
        for (Mixer.Info info : mixerInfo) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                mixer.open();
                Line.Info[] targetLines = mixer.getTargetLineInfo();


                for (Line.Info targetLineInfo : targetLines) {
                    try {
                        TargetDataLine line = (TargetDataLine) mixer.getLine(targetLineInfo);
                        line.open();
                        line.close();
                    } catch (Exception e) {
                        continue;
                    }
                    userAudioOptions.add(new UserAudioOption(mixer, targetLineInfo, info.getName() + " " + targetLineInfo.toString()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return userAudioOptions;
    }

    public void setGain(float value) {
        audioSamplerThread.setGain(value);
    }

    public DisplayBuffer getDisplayBuffer() {
        return displayBuffer;
    }

    public Double getRmsValue() {
        return audioSamplerThread.getRms();
    }

    public void setFilterLength(int value) {
        if (midiThread != null)
            midiThread.setFilterLength(value);
    }

    public void setMidiSamplePeriod(int value) {
        midiThread.setPeriod(value);
    }

    public void setQuantize(int value) {
        if (midiThread != null)
            midiThread.setQuantize(value);
    }

    public void setDelay(int value) {
        if (midiThread != null)
            midiThread.setDelay(value);
    }
}