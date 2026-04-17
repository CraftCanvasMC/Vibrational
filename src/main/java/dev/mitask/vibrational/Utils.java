package dev.mitask.vibrational;

// https://github.com/henkelmax/voicechat-interaction/blob/master/src/main/java/de/maxhenkel/vcinteraction/AudioUtils.java
public class Utils {
    /**
     * Calculates the audio level of a signal with specific samples.
     *
     * @author Henhelmax
     * @param samples the samples of the signal to calculate the audio level of
     * @return the audio level of the specified signal in db
     */
    public static double calculateAudioLevel(short[] samples) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (short value : samples) {
            double sample = (double) value / (double) Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = samples.length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        if(rms <= 0D) return -127D;
        // dB
        return Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
    }
}
