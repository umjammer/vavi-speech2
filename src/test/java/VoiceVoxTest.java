import vavi.speech.voicevox.VoiceVox;


public class VoiceVoxTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("vavi.speech.voicevox.VoiceVox.bundlePath", "tmp/macos-x64");
        VoiceVox voiceVox = VoiceVox.getInstance();
        while (true) {
            Thread.yield();
        }
    }
}
