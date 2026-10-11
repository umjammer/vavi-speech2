/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicestudio;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import static java.lang.System.getLogger;


/**
 * VoiceStudio.
 * <p>
 * uses the VoiceStudio backend (OpenAI compatible audio api + VoiceStudio extensions).
 * <p>
 * system property:
 * <li>{@code vavi.speech.voicestudio.url} ... the api server url, default is {@code http://127.0.0.1:3900}</li>
 * <li>{@code vavi.speech.voicestudio.language} ... speaking language (ISO 639-1), default is auto detection</li>
 * <li>{@code vavi.speech.voicestudio.clone} ... use clone voice by reference audio or not, default is {@code false}</li>
 * <li>{@code vavi.speech.voicestudio.refAudio} ... when using clone voice, reference audio file path (wav, mp3, ...)</li>
 * <li>{@code vavi.speech.voicestudio.refText} ... when using clone voice, transcription of the reference audio</li>
 * <p/>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-11 nsano initial version <br>
 */
public class VoiceStudio implements Closeable {

    private static final Logger logger = getLogger(VoiceStudio.class.getName());

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
        String url = System.getProperty("vavi.speech.voicestudio.url", null);
        if (url == null || !url.startsWith("http:")) {
            return "http://127.0.0.1:3900";
        } else {
            return url;
        }
    }

    /** */
    public VoiceStudio() {
        this(getUrl());
    }

    /** */
    public VoiceStudio(String url) {
        try {
            client = ClientBuilder.newClient(); // DON'T CLOSE
            logger.log(Level.DEBUG, "url: " + url);
            target = client.target(url);

            String response = target.path("health")
                    .request().get(String.class);
            logger.log(Level.DEBUG, "response: " + response);
        } catch (Exception e) {
            throw new IllegalStateException("VoiceStudio is not available at " + url, e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /** POST /v1/audio/speech */
    public static class AudioSpeechQuery {
        /** null means the active engine */
        String model;
        String input;
        /** voice id (profile id, 'default', preset name or openai alias) */
        String voice;
        String response_format = "wav";
        float speed = 1;
        String language;
        String instruct;
        @Override public String toString() {
            return "AudioSpeechQuery{" +
                    "model=" + model +
                    ", input=" + input +
                    ", voice=" + voice +
                    ", response_format=" + response_format +
                    ", speed=" + speed +
                    ", language=" + language +
                    ", instruct=" + instruct +
                    '}';
        }
        /** @param speed default: 1, range: 0.25 ~ 4.00 */
        public void setSpeed(float speed) {
            if (0.25 <= speed && speed <= 4.0)
                this.speed = speed;
            else
                this.speed = 1;
        }
    }

    final boolean clone = System.getProperty("vavi.speech.voicestudio.clone", "false").equals("true");

    final String refAudio = System.getProperty("vavi.speech.voicestudio.refAudio", "");
    final String refText = System.getProperty("vavi.speech.voicestudio.refText", "");

    /** @return null when not specified (auto detection) */
    private static String getLanguage() {
        String language = System.getProperty("vavi.speech.voicestudio.language");
        return language == null || language.isEmpty() || language.equalsIgnoreCase("auto") ? null : language;
    }

    /** simple multipart/form-data builder */
    private static class Multipart {
        final String boundary = "----vavi" + UUID.randomUUID().toString().replace("-", "");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Multipart field(String name, String value) {
            if (value != null) {
                write("--" + boundary + "\r\n");
                write("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
                write(value + "\r\n");
            }
            return this;
        }
        Multipart file(String name, Path path) throws IOException {
            write("--" + boundary + "\r\n");
            write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + path.getFileName() + "\"\r\n");
            write("Content-Type: application/octet-stream\r\n\r\n");
            baos.write(Files.readAllBytes(path));
            write("\r\n");
            return this;
        }
        Entity<byte[]> build() {
            write("--" + boundary + "--\r\n");
            return Entity.entity(baos.toByteArray(), MediaType.MULTIPART_FORM_DATA + "; boundary=" + boundary);
        }
        private void write(String s) {
            baos.writeBytes(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** clone voice by a reference audio, POST /generate */
    private InputStream synthesizeClone(String input) throws IOException {
        Path path = Path.of(refAudio);
        if (!Files.exists(path)) throw new IOException("reference audio not found: " + refAudio);
        Multipart multipart = new Multipart()
                .field("text", input)
                .field("language", getLanguage())
                .field("ref_text", refText.isEmpty() ? null : refText)
                .file("ref_audio", path);
logger.log(Level.TRACE, "clone: " + input + ", ref: " + path);
        return target.path("generate")
                .request().post(multipart.build(), InputStream.class);
    }

    /** voice by id (profile voices are cloned voices also), POST /v1/audio/speech */
    private InputStream synthesizeNormal(String input, String voice) throws IOException {
        AudioSpeechQuery audioQuery = new AudioSpeechQuery();
        audioQuery.language = getLanguage();
        audioQuery.input = input;
        audioQuery.voice = voice;
logger.log(Level.TRACE, audioQuery);
        Entity<String> entity = Entity.entity(gson.toJson(audioQuery), MediaType.APPLICATION_JSON);
        return target.path("v1/audio/speech")
                .request().post(entity, InputStream.class);
    }

    /**
     * @param voice voice id
     * @return wave stream
     */
    public InputStream synthesize(String input, String voice) throws IOException {
        if (clone) {
            return synthesizeClone(input);
        } else {
            return synthesizeNormal(input, voice);
        }
    }

    /** */
    static class VoiceResponse {
        Voice[] voices;
    }

    /** */
    public static class Voice {
        public String voice_id;
        public String name;
        /** "openai_alias", "profile", "preset", ... */
        public String type;
        public String language;
        public String description;
        @Override public String toString() {
            return "Voice{" +
                    "voice_id='" + voice_id + '\'' +
                    ", name='" + name + '\'' +
                    ", type=" + type +
                    ", language=" + language +
                    ", description=" + description +
                    '}';
        }
    }

    /** GET /v1/audio/voices */
    public Voice[] getAllVoices() {
        if (voices == null) {
            String voicesJson = target
                    .path("v1/audio/voices")
                    .request()
                    .get(String.class);

            VoiceResponse response = gson.fromJson(voicesJson, VoiceResponse.class);
            voices = response.voices;
logger.log(Level.TRACE, "voices: " + voices.length);
        }
        return Arrays.copyOf(voices, voices.length);
    }
}
