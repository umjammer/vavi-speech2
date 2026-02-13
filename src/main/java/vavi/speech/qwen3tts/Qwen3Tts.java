/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.qwen3tts;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import static java.lang.System.getLogger;


/**
 * Qwen3-TTS.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-02-13 nsano initial version <br>
 */
public class Qwen3Tts implements Closeable {

    private static final Logger logger = getLogger(Qwen3Tts.class.getName());

    /** rest response parser */
    private static final Gson gson = new GsonBuilder().create();

    /** rest target */
    private final WebTarget target;

    /** voices */
    private Voice[] voices;

    /** rest client */
    private final Client client;

    /** server url */
    private static String getUrl() {
        String url = System.getProperty("vavi.speech.voicevox.url", null);
        if (url == null || !url.startsWith("http:")) {
            return "http://localhost:50030";
        } else {
            return url;
        }
    }

    /** */
    public Qwen3Tts() {
        this(getUrl());
    }

    /** */
    public Qwen3Tts(String url) {
        try {
            client = ClientBuilder.newClient(); // DON'T CLOSE
            logger.log(Level.DEBUG, "url: " + url);
            target = client.target(url);

            String response = target.path("health")
                    .request().get(String.class);
            logger.log(Level.DEBUG, "response: " + response);
        } catch (Exception e) {
            throw new IllegalStateException("Qwen3-TTS is not available at " + url, e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /** */
    public static class NormalizationOptions {
        boolean normalize = true;
        boolean unit_normalization = true;
        boolean url_normalization = true;
        boolean email_normalization = true;
        boolean optional_pluralization_normalization = true;
        boolean phone_normalization = true;
        boolean replace_remaining_symbols = true;
        @Override public String toString() {
            return "NormalizationOptions{" +
                    "normalize=" + normalize +
                    ", unit_normalization=" + unit_normalization +
                    ", url_normalization=" + url_normalization +
                    ", email_normalization=" + email_normalization +
                    ", optional_pluralization_normalization=" + optional_pluralization_normalization +
                    ", phone_normalization=" + phone_normalization +
                    ", replace_remaining_symbols=" + replace_remaining_symbols +
                    '}';
        }
    }

    /** */
    public static class AudioSpeechQuery {
        public String model = "qwen3-tts";
        public String input;
        String voice;
        public String response_format = "wav";
        int speed = 1;
        boolean stream = false;
        String language = "Auto";
        String instruct = "";
        NormalizationOptions normalizationOptions;
        @Override public String toString() {
            return "AudioSpeechQuery{" +
                    "model=" + model +
                    ", input=" + input +
                    ", voice=" + voice +
                    ", response_format=" + response_format +
                    ", speed=" + speed +
                    ", stream=" + stream +
                    ", language=" + language +
                    ", instruct=" + instruct +
                    ", normalizationOptions=" + normalizationOptions +
                    '}';
        }
        /** @param speed default: 1, range: 0.50 ~ 2.00 */
        public void setSpeed(float speed) {
            if (0.5 <= speed && speed <= 2.0)
                this.speed = (int) speed;
            else
                this.speed = 1;
        }
    }

    /** */
    public static class AudioVoiceCloneQuery {
        public String input;
        String ref_audio;
        String ref_text;
        boolean x_vector_only_mode;
        String language = "Auto";
        public String response_format = "wav";
        int speed = 1;
        NormalizationOptions normalizationOptions;
        @Override public String toString() {
            return "AudioVoiceCloneQuery{" +
                    "input=" + input +
                    ", ref_audio=" + ref_audio +
                    ", ref_text=" + ref_text +
                    ", x_vector_only_mode=" + x_vector_only_mode +
                    ", language=" + language +
                    ", response_format=" + response_format +
                    ", speed=" + speed +
                    ", normalizationOptions=" + normalizationOptions +
                    '}';
        }
        /** @param speed default: 1, range: 0.50 ~ 2.00 */
        public void setSpeed(float speed) {
            if (0.5 <= speed && speed <= 2.0)
                this.speed = (int) speed;
            else
                this.speed = 1;
        }
    }

    boolean clone = System.getProperty("vavi.speech.qwen3tts.clone", "false").equals("true");

    Path path = Path.of(System.getProperty("vavi.speech.qwen3tts.ref", ""));

    /** */
    public InputStream synthesize2(String input, String voice) throws IOException {
        AudioVoiceCloneQuery audioQuery = new AudioVoiceCloneQuery();
        audioQuery.input = input;
        audioQuery.ref_audio = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        Entity<String> entity = Entity.entity(gson.toJson(audioQuery), MediaType.APPLICATION_JSON);
        String response = target.path("v1/audio/voice-clone")
                .request().post(entity, String.class);
logger.log(Level.INFO, response);
        return null;
    }

    /** */
    public InputStream synthesize(String input, String voice) throws IOException {
        if (clone) {
            return synthesize2(input, voice);
        } else {
            AudioSpeechQuery audioQuery = new AudioSpeechQuery();
            audioQuery.input = input;
            audioQuery.voice = voice;
            audioQuery.instruct = "female anime voice";
            Entity<String> entity = Entity.entity(gson.toJson(audioQuery), MediaType.APPLICATION_JSON);
            return target.path("v1/audio/speech")
                    .request().post(entity, InputStream.class);
        }
    }

    /** */
    static class VoiceResponse {
        Voice[] voices;
        String[] languages;
    }

    /** */
    public static class Voice {
        public String id;
        public String name;
        public String language;
        public String description;
        @Override public String toString() {
            return "Speaker{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", language=" + language +
                    ", description=" + description +
                    '}';
        }
    }

    /** */
    public Voice[] getAllVoices() {
        //
        if (voices == null) {
            String speakersJson = target
                    .path("v1/voices")
                    .request()
                    .get(String.class);

            VoiceResponse response = gson.fromJson(speakersJson, VoiceResponse.class);
            voices = response.voices;
logger.log(Level.TRACE, Arrays.toString(response.languages));
        }
        return Arrays.stream(voices).toArray(Voice[]::new);
    }
}
