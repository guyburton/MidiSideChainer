import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainPanel extends JPanel implements Runnable, ChangeListener {

    private static final int MIDI_SAMPLE_PERIOD_MS = 50;
    private static final int DEFAULT_FILTER = 5;
    private static final int DEFAULT_QUANTIZE = 1;
    private static final int DEFAULT_SAMPLE_PERIOD = 120;
    private static final int DEFAULT_DELAY = 1;
    public static final int DEFAULT_GAIN = 600;

    private final MainSetup mainSetup;
    private final JLabel label;
    private final JSlider gainSlider;
    private final JSlider midiSampleSlider;
    private final JSlider filterSlider;
    private final JSlider quantizeSlider;
    private final JSlider delaySlider;
    private final JLabel delayLabel;
    private final JLabel quantizeLabel;
    private final JLabel filterLabel;
    private final JLabel sampleRateLabel;
    private final JLabel gainLabel;

    public MainPanel(MainSetup mainSetup) {
        super(new BorderLayout());
        this.mainSetup = mainSetup;

        JPanel selectorPanel = new JPanel(new GridLayout(2,1));
        List<UserAudioOption> audioInterfaces = mainSetup.getAudioInterfaces();
        JComboBox interfaceCombo = new JComboBox<>(audioInterfaces.toArray());
        selectorPanel.add(interfaceCombo);
        interfaceCombo.addActionListener(e -> setAudioInterface((UserAudioOption) interfaceCombo.getSelectedItem()));
        JComboBox midiCombo = new JComboBox<>(mainSetup.getMidiInterfaces().toArray());
        selectorPanel.add(midiCombo);
        add(selectorPanel, BorderLayout.NORTH);

        JPanel controlsContainer = new JPanel(new GridLayout(6,2));
        controlsContainer.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        label = new JLabel("RMS: 0");
        controlsContainer.add(label);

        JToggleButton invert = new JToggleButton("Invert");
        controlsContainer.add(invert);

        gainLabel = new JLabel("Gain: ");
        controlsContainer.add(gainLabel);
        gainSlider = new JSlider(0, 2000);
        gainSlider.setValue(DEFAULT_GAIN);
        gainSlider.addChangeListener(this);
        controlsContainer.add(gainSlider);

        sampleRateLabel = new JLabel("Midi Sample Rate: ");
        controlsContainer.add(sampleRateLabel);

        midiSampleSlider = new JSlider(20,1000);
        midiSampleSlider.setValue(DEFAULT_SAMPLE_PERIOD);
        midiSampleSlider.addChangeListener(this);
        controlsContainer.add(midiSampleSlider);

        filterLabel = new JLabel("Filter: ");
        controlsContainer.add(filterLabel);

        filterSlider = new JSlider(1,30);
        filterSlider.setValue(DEFAULT_FILTER);
        filterSlider.addChangeListener(this);
        controlsContainer.add(filterSlider);

        quantizeLabel = new JLabel("Quantize: ");
        controlsContainer.add(quantizeLabel);

        quantizeSlider = new JSlider(1,40);
        quantizeSlider.setValue(DEFAULT_QUANTIZE);
        quantizeSlider.addChangeListener(this);
        controlsContainer.add(quantizeSlider);

        delayLabel = new JLabel("Delay: ");
        controlsContainer.add(delayLabel);

        delaySlider = new JSlider(0,10);
        delaySlider.setValue(DEFAULT_DELAY);
        delaySlider.addChangeListener(this);
        controlsContainer.add(delaySlider);

        VisualisationPanel visualisationPanel = new VisualisationPanel(mainSetup.getDisplayBuffer());
        visualisationPanel.setPreferredSize(new Dimension(getWidth(), 600));

        add(controlsContainer, BorderLayout.CENTER);
        add(visualisationPanel, BorderLayout.SOUTH);

        invert.addActionListener(e -> mainSetup.setInverted(invert.isSelected()));
        midiCombo.addActionListener(setMidiInterface(mainSetup, midiCombo));

        setAudioInterface(audioInterfaces.get(0));

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(MainPanel.this, 100, MIDI_SAMPLE_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    private void updateAll()
    {
        mainSetup.setGain(gainSlider.getValue() / 100f);
        gainLabel.setText("Gain: " + gainSlider.getValue() / 100f);
        mainSetup.setQuantize(quantizeSlider.getValue());
        quantizeLabel.setText("Quantize: " + quantizeSlider.getValue());
        mainSetup.setMidiSamplePeriod(midiSampleSlider.getValue());
        sampleRateLabel.setText("Midi Sample Rate: " + midiSampleSlider.getValue() + "ms");
        mainSetup.setDelay(delaySlider.getValue());
        delayLabel.setText("Delay: " + delaySlider.getValue());
        mainSetup.setFilterLength(filterSlider.getValue());
        filterLabel.setText("Filter: " + filterSlider.getValue());
    }

    private ActionListener setMidiInterface(MainSetup mainSetup, JComboBox midiCombo) {
        return e -> {
            try {
                mainSetup.setMidiInterface((UserMidiOption) midiCombo.getSelectedItem());
                updateAll();
            } catch (Exception ex) {
                handleError(ex);
            }
        };
    }

    private void setAudioInterface(UserAudioOption selectedItem) {
        try {
            mainSetup.setAudioInterface(selectedItem);
        } catch (Exception ex) {
            handleError(ex);
        }
    }

    private void handleError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void run() {
        Double rmsValue = mainSetup.getRmsValue();
        if (rmsValue == null)
            return;
        SwingUtilities.invokeLater(() -> label.setText("RMS: " + String.format("%.4f", rmsValue)));
        repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updateAll();
    }
}
