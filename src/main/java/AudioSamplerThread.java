import com.sun.media.sound.AudioFloatConverter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

public class AudioSamplerThread extends Thread {

    public static final int READ_BUFFER_SIZE = 512;
    private final TargetDataLine targetLine;
    private volatile double rms;
    private volatile boolean stop;
    private volatile float gain = 1;

    public AudioSamplerThread(TargetDataLine targetLine) {
        this.targetLine = targetLine;
        try {
            targetLine.open();
            targetLine.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws InterruptedException {
        targetLine.stop();
        targetLine.close();

        stop = true;
        super.join();
    }

    @Override
    public void run() {
        AudioFormat format = targetLine.getFormat();
        System.out.println("Sampling from " + targetLine.getLineInfo().toString() + " with " + format);
        AudioFloatConverter converter = AudioFloatConverter.getConverter(format);
        AudioInputStream stream = new AudioInputStream(targetLine);
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        byte[] buffer = new byte[READ_BUFFER_SIZE * bytesPerSample * format.getChannels()];
        float[] floats = new float[READ_BUFFER_SIZE];
        while (!stop) {
            try {
                if (stream.available() < buffer.length) {
                    Thread.yield();
                    continue;
                }
                stream.read(buffer, 0, buffer.length);
                converter.toFloatArray(buffer, floats);
                float total = 0;
                int num = 0;
                for (float sample : floats) {
                    total += sample * sample;
                    num++;
                }
                this.rms = Math.sqrt(total / num) * gain;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public double getRms() {
        return rms;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }
}
