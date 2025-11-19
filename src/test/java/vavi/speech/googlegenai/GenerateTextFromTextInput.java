/*
 * https://ai.google.dev/gemini-api/docs/quickstart?lang=python&hl=ja#java_1
 */

package vavi.speech.googlegenai;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent.Type;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.PrebuiltVoiceConfig;
import com.google.genai.types.SpeechConfig;
import com.google.genai.types.VoiceConfig;
import vavi.util.Debug;
import vavi.util.StringUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;


/**
 * The client gets the API key from the environment variable `GOOGLE_API_KEY`.
 */
@EnabledIfEnvironmentVariable(named = "GOOGLE_API_KEY", matches = ".*")
public class GenerateTextFromTextInput {

    @Test
    @DisplayName("raw api")
    void test1() throws Exception {
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash-preview-tts",
                        "Say rap style:" +
                                "Started from the bottom and I never looked back." +
                                " Look at all my haters, yeah I'm makin' 'em mad." +
                                "I'm ready for a fight, when it's on it's on." +
                                "I'm charging like a bull, when it's on it's on." +
                                "When it's all said and done they gon' call me a legend." +
                                "I'd love to break bread but I'd rather break records.",
                        GenerateContentConfig.builder()
                                .responseModalities(List.of("AUDIO"))
                                .speechConfig(SpeechConfig.builder()
                                        .voiceConfig(VoiceConfig.builder()
                                                .prebuiltVoiceConfig(PrebuiltVoiceConfig.builder()
                                                        .voiceName("Kore")
                                                        .build())
                                                .build())
                                        .build())
                                .build());

        byte[] data = response
                .candidates().orElseThrow().getFirst()
                .content().orElseThrow()
                .parts().orElseThrow().getFirst()
                .inlineData().orElseThrow()
                .data().orElseThrow();
Debug.println(data.length + " bytes\n" + StringUtil.getDump(data, 128));

        AudioFormat af = new AudioFormat(24000.0f, 16, 1, true, false);

        CountDownLatch cdl = new CountDownLatch(1);

        Clip clip = AudioSystem.getClip();
        clip.addLineListener(e -> { if (e.getType() == Type.STOP) cdl.countDown(); });
        clip.open(new AudioInputStream(new ByteArrayInputStream(data), af, -1));
        clip.start();

        cdl.await();

        clip.drain();
        clip.stop();
        client.close();
    }

    /** */
    public static void main(String[] args) {
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.0-flash",
                        "Explain how AI works in a few words",
                        null);

        System.out.println(response.text());

        client.close();
    }
}
