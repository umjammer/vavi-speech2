/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicevox;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.speech.voicevox.VoiceVox.Speaker;
import vavi.speech.voicevox.VoiceVox.AudioQuery;
import vavi.speech.voicevox.VoiceVox.SpeakerInfo;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * VoiceVoxTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-12 nsano initial version <br>
 */
class VoiceVoxTest {

    private WebTarget target;

    private Client c;

    @BeforeEach
    void setUp() throws Exception {
        c = ClientBuilder.newClient();
        target = c.target("http://localhost:50021/");
    }

    @AfterEach
    void tearDown() throws Exception {
        c.close();
    }

    @Test
    @DisplayName("raw rest api")
    void test1() throws Exception {
        String text = "ひざまずくが良いのだ、この愚かな地球人共よ";

        int speakerId = 1; // ずんだもん(あまあま)

        String query = target
                .path("audio_query")
                .queryParam("text", text)
                .queryParam("speaker", 1)
                .request()
                .post(null, String.class);
Debug.println("audio_query:\n" + query);

        AudioQuery audioQuery = gson.fromJson(query, AudioQuery.class);
Debug.println("audioQuery: " + audioQuery);
        audioQuery.speedScale = 1.2f;
        audioQuery.volumeScale = .2f;

        Entity<String> entity = Entity.entity(gson.toJson(audioQuery), MediaType.APPLICATION_JSON);
        InputStream wav = target
                .path("synthesis")
                .queryParam("speaker", 1)
                .request()
                .post(entity, InputStream.class);
        speak(wav);
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

    static Gson gson = new GsonBuilder().create();

    @Test
    void test2() throws Exception {
        //
        String version = target
                .path("version")
                .request()
                .get(String.class);
Debug.println("version: " + version);

        //
        String presets = target
                .path("presets")
                .request()
                .get(String.class);
Debug.println("presets: " + presets);

        //
        String speakersJson = target
                .path("speakers")
                .request()
                .get(String.class);
//Debug.println("speakers: " + speakers);

        Speaker[] speakers = gson.fromJson(speakersJson, Speaker[].class);
Arrays.stream(speakers).forEach(System.err::println);

        Arrays.stream(speakers).forEach(s -> {
            String speakerInfoJson = target
                    .path("speaker_info")
                    .queryParam("speaker_uuid", s.speaker_uuid)
                    .request()
                    .get(String.class);
            SpeakerInfo speakerInfo = gson.fromJson(speakerInfoJson, SpeakerInfo.class);
Debug.println("SpeakerInfo: " + speakerInfo);
        });
    }

    @Test
    @DisplayName("use wrapped api")
    void test3() throws Exception {
        int speakerId = 3; // ずんだもん(ノーマル)
        VoiceVox voiceVox = new VoiceVox();
        AudioQuery audioQuery = voiceVox.getQuery("ひざまずくが良いのだ、この愚かな地球人共よ", speakerId);
        audioQuery.setSpeed(1.1f);
        audioQuery.setVolume(0.2f);
        speak(voiceVox.synthesize(audioQuery, speakerId));
        voiceVox.close();
    }

    @Test
    @DisplayName("wrapped api list voices")
    void test4() throws Exception {
        VoiceVox voiceVox = new VoiceVox();
        Voice[] voices = voiceVox.getAllVoices();
Arrays.stream(voices).forEach(System.err::println);
        assertEquals(22, voiceVox.getId(voices[10]));
        voiceVox.close();
    }

    @Test
    @DisplayName("search レキシカ voice and parameter. is he or she really virtual voice?")
    void test5() throws Exception {
        String text = "宇宙はバチクソ面白いので";

        int speakerId = 43;  // 櫻歌ミコ(ノーマル)

        String query = target
                .path("audio_query")
                .queryParam("text", text)
                .queryParam("speaker", speakerId)
                .request()
                .post(null, String.class);
Debug.println("audio_query:\n" + query);

        AudioQuery audioQuery = gson.fromJson(query, AudioQuery.class);
Debug.println("audioQuery: " + audioQuery);
        audioQuery.speedScale = 0.97f;
        audioQuery.pitchScale = .07f;
        audioQuery.volumeScale = .2f;

        Entity<String> entity = Entity.entity(gson.toJson(audioQuery), MediaType.APPLICATION_JSON);
        InputStream wav = target
                .path("synthesis")
                .queryParam("speaker", speakerId)
                .request()
                .post(entity, InputStream.class);
        speak(wav);
    }
}
