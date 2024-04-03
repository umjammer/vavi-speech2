/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.coeiroink;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.speech.synthesis.Voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.speech.coeiroink.CoeiroInk;
import vavi.speech.coeiroink.CoeiroInk.EngineInfo;
import vavi.speech.coeiroink.CoeiroInk.Prosody;
import vavi.speech.coeiroink.CoeiroInk.Speaker;
import vavi.speech.coeiroink.CoeiroInk.Synthesis;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static vavi.speech.coeiroink.CoeiroInk.execWithBackoff;


/**
 * CoeiroInkTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-04-22 nsano initial version <br>
 */
@Disabled("CoeiroInk doesn't support wev api yet")
class CoeiroInkTest {

    private WebTarget target;

    @BeforeEach
    public void setUp() throws Exception {
        Client c = ClientBuilder.newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
        target = c.target("http://127.0.0.1:50032/");
    }

    @Test
    @DisplayName("raw rest api")
    void test1() throws Exception {
        String text = "これはテストです。";

        String speakerUuid = "3c37646f-3881-5374-2a83-149267990abc"; // つくよみちゃん
        int styleId = 0; // (れいせい)

        Entity<String> entity = Entity.entity("{ \"text\": \"" + text + "\"}", MediaType.APPLICATION_JSON);
        String json = target
                .path("v1/estimate_prosody")
                .request()
                .post(entity, String.class);
        Prosody prosody = gson.fromJson(json, Prosody.class);
Debug.println(prosody);

        Synthesis synthesis = new Synthesis();
        synthesis.speakerUuid = speakerUuid;
        synthesis.styleId = styleId;
        synthesis.text = text;
        synthesis.prosodyDetail = prosody.detail;
        synthesis.speedScale = 1;
        synthesis.volumeScale = 1;
        synthesis.pitchScale = 0;
        synthesis.intonationScale = 1;
        synthesis.prePhonemeLength = 0.1f;
        synthesis.postPhonemeLength = 0.1f;
        synthesis.outputSamplingRate = 22050;

        Entity<String> syntheEntity = Entity.entity(gson.toJson(synthesis), MediaType.APPLICATION_JSON);
        Response response = execWithBackoff(() -> target
                .path("v1/synthesis")
                .request()
                .post(syntheEntity));
        InputStream wav = response.readEntity(InputStream.class);
        speak(wav);
        response.close();
    }

    /** */
    static void speak(InputStream is) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
        DataLine.Info line = new DataLine.Info(Clip.class, ais.getFormat());
        Clip clip = (Clip) AudioSystem.getLine(line);
        CountDownLatch cdl = new CountDownLatch(1);
        clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) cdl.countDown(); });
        clip.open(ais);
        clip.start();
        cdl.await();
        clip.stop();
        clip.close();
    }

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    void test2() throws Exception {
        //
        String json = target
                .path("/v1/engine_info")
                .request()
                .get(String.class);
        EngineInfo engineInfo = gson.fromJson(json, EngineInfo.class);
Debug.println("version: " + engineInfo.version);

        //
        String speakersJson = target
                .path("/v1/speakers")
                .request()
                .get(String.class);
//Debug.println("speakers: " + speakers);

        Speaker[] speakers = gson.fromJson(speakersJson, Speaker[].class);
Arrays.stream(speakers).forEach(System.err::println);
    }

    @Test
    @DisplayName("use wrapped api")
    void test3() throws Exception {
        String speakerUuid = "3c37646f-3881-5374-2a83-149267990abc"; // つくよみちゃん
        int styleId = 0; // (れいせい)
        CoeiroInk coeiroInk = new CoeiroInk();
        String text = "この音声はコエイロインクにより発声されたものです";
        Prosody prosody = coeiroInk.getProsody(text);
        Synthesis synthesis = new Synthesis();
        synthesis.speakerUuid = speakerUuid;
        synthesis.styleId = styleId;
        synthesis.text = text;
        synthesis.prosodyDetail = prosody.detail;
        speak(coeiroInk.synthesize(synthesis));
        coeiroInk.close();
    }

    @Test
    @DisplayName("wrapped api list voices")
    void test4() throws Exception {
        CoeiroInk coeiroInk = new CoeiroInk();
        Speaker[] voices = coeiroInk.getAllVoices();
Arrays.stream(voices).forEach(System.err::println);
        assertEquals("3c37646f-3881-5374-2a83-149267990abc", voices[0].speakerUuid);
        coeiroInk.close();
    }
}
