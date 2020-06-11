import javax.sound.sampled.*;

public class Voice extends Thread {

    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;

    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    public void setTargetDataLine(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;
    }

    public void setSourceDataLine(SourceDataLine sourceDataLine) {
        this.sourceDataLine = sourceDataLine;
    }

    public AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public synchronized SourceDataLine getSourceDataLine() {
        return this.sourceDataLine;
    }

    public synchronized TargetDataLine getTargetDataLine() {
        return this.targetDataLine;
    }

    public void setupOutput() throws LineUnavailableException {

        setAudioFormat(getAudioFormat());
        DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, this.audioFormat);

        setSourceDataLine((SourceDataLine) AudioSystem.getLine(dataLineInfo1));

        getSourceDataLine().open(audioFormat);
        getSourceDataLine().start();

        //Setting the volume
        FloatControl control = (FloatControl) getSourceDataLine().getControl(FloatControl.Type.MASTER_GAIN);
        control.setValue(control.getMaximum() / 2);


    }

    public synchronized void captureAudio() {
        try {
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();    //get available mixers
            System.out.println("Available mixers:");
            Mixer mixer = null;
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(cnt + " " + mixerInfo[cnt].getName());
                mixer = AudioSystem.getMixer(mixerInfo[cnt]);

                Line.Info[] lineInfos = mixer.getTargetLineInfo();
                if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                    System.out.println(cnt + " Mic is supported!");
                    break;
                }
            }

            audioFormat = getAudioFormat();     //get the audio format
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            assert mixer != null;
            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}