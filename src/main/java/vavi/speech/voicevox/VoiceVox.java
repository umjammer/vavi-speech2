/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicevox;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import static java.lang.System.getLogger;


/**
 * VoiceVox.
 * <p>
 * system property
 * <li>vavi.speech.voicevox.url ... VoiceVox REST api url, default is "http://localhost:50021/"</li>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-14 nsano initial version <br>
 */
public class VoiceVox implements Closeable {

    private static final Logger logger = getLogger(VoiceVox.class.getName());

    /** rest response parser */
    private static final Gson gson = new GsonBuilder().create();

    /** rest target */
    private final WebTarget target;

    /** voices */
    private Speaker[] speakers;

    /** rest client */
    private final Client client;

    /** server url */
    private static String getUrl() {
        String url = System.getProperty("vavi.speech.voicevox.url", null);
        if (url == null || !url.startsWith("http:")) {
            return "http://localhost:50021/";
        } else {
            return url;
        }
    }

    /** */
    public VoiceVox() {
        this(getUrl());
    }

    /** */
    public VoiceVox(String url) {
        try {
            client = ClientBuilder.newClient(); // DON'T CLOSE
logger.log(Level.DEBUG, "url: " + url);
            target = client.target(url);

            String version = target.path("version")
                    .request().get(String.class);
logger.log(Level.DEBUG, "version: " + version);
        } catch (Exception e) {
            throw new IllegalStateException("VoiceVox is not available at " + url, e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /** */
    public static class AudioQuery {
        public static class AccentPhrase {
            public static class Mora{
                String text;
                String consonant;
                float consonant_length;
                String vowel;
                float vowel_length;
                float pitch;
                @Override public String toString() {
                    return "Mora{" +
                            "text='" + text + '\'' +
                            ", consonant='" + consonant + '\'' +
                            ", consonant_length=" + consonant_length +
                            ", vowel='" + vowel + '\'' +
                            ", vowel_length=" + vowel_length +
                            ", pitch=" + pitch +
                            '}';
                }
            }
            Mora[] moras;
            int accent;
            Mora pause_mora;
            boolean is_interrogative;
            @Override public String toString() {
                return "AccentPhrase{" +
                        "moras=" + Arrays.toString(moras) +
                        ", accent=" + accent +
                        ", pause_mora=" + pause_mora +
                        ", is_interrogative=" + is_interrogative +
                        '}';
            }
        }
        AccentPhrase[] accent_phrases;
        public float speedScale;
        public float pitchScale;
        float intonationScale;
        public float volumeScale;
        float prePhonemeLength;
        float postPhonemeLength;
        int outputSamplingRate;
        boolean outputStereo;
        String kana;
        @Override public String toString() {
            return "AudioQuery{" +
                    "accent_phrases=" + Arrays.toString(accent_phrases) +
                    ", speedScale=" + speedScale +
                    ", pitchScale=" + pitchScale +
                    ", intonationScale=" + intonationScale +
                    ", volumeScale=" + volumeScale +
                    ", prePhonemeLength=" + prePhonemeLength +
                    ", postPhonemeLength=" + postPhonemeLength +
                    ", outputSamplingRate=" + outputSamplingRate +
                    ", outputStereo=" + outputStereo +
                    ", kana='" + kana + '\'' +
                    '}';
        }
        /** @param speed default: 1, range: 0.50 ~ 2.00 */
        public void setSpeed(float speed) {
            if (0.5 <= speed && speed <= 2.0)
                speedScale = speed;
            else
                speedScale = 1;
        }
        /** @param pitch default: 0, range: -0.15 ~ 0.15 */
        public void setPitch(float pitch) {
            if (-0.15 <= pitch && pitch <= 0.15)
                pitchScale = pitch;
            else
                pitchScale = 0;
        }
        /** @param intonation range: 0 ~ 2 */
        public void setIntonation(float intonation) {
            if (0 <= intonation && intonation <= 2)
                intonationScale = intonation;
            else
                intonationScale = 1;
        }
        /** @param volume default: 1, range: 0.50 ~ 2.00 */
        public void setVolume(float volume) {
            if (0.5 <= volume && volume <= 2.0)
                volumeScale = volume;
            else
                volumeScale = 1;
        }
    }

    /** */
    public AudioQuery getQuery(String text, int speakerId) {
        String query = target.path("audio_query")
                .queryParam("text", text)
                .queryParam("speaker", speakerId)
                .request().post(null, String.class);

        return gson.fromJson(query, AudioQuery.class);
    }

    /** */
    public InputStream synthesize(AudioQuery audioQuery, int speakerId) {
        Entity<String> entity = Entity.entity(gson.toJson(audioQuery), MediaType.APPLICATION_JSON);
        return target.path("synthesis")
                .queryParam("speaker", speakerId)
                .request().post(entity, InputStream.class);
    }

    /** */
    public static class Speaker {
        String name;
        public String speaker_uuid;
        public static class Style {
            int id;
            String name;
            @Override public String toString() {
                return "Style{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        '}';
            }
        }
        Style[] styles;
        String version;
        @Override public String toString() {
            return "Speaker{" +
                    "name='" + name + '\'' +
                    ", speaker_uuid='" + speaker_uuid + '\'' +
                    ", styles=" + Arrays.toString(styles) +
                    ", version=" + version +
                    '}';
        }
    }

    /** */
    public static class SpeakerInfo {
        String policy;
        String portrait;
        public static class StyleInfo {
            int id;
            String icon;
            String[] voice_samples;
            @Override public String toString() {
                return "StyleInfo{" +
                        "id=" + id +
//                        ", icon='" + icon + '\'' +
//                        ", voice_samples=" + Arrays.toString(voice_samples) +
                        '}';
            }
        }
        StyleInfo[] style_infos;
        @Override public String toString() {
            return "SpeakerInfo{" +
                    "policy='" + policy + '\'' +
//                    ", portrait='" + portrait + '\'' +
                    ", style_infos=" + Arrays.toString(style_infos) +
                    '}';
        }
    }

    /** */
    public Voice[] getAllVoices() {
        //
        if (speakers == null) {
            String speakersJson = target
                    .path("speakers")
                    .request()
                    .get(String.class);

            speakers = gson.fromJson(speakersJson, Speaker[].class);
        }
        SpeechLocale japan = new SpeechLocale(Locale.JAPANESE.toString());
        return Arrays.stream(speakers).flatMap(speaker -> Arrays.stream(speaker.styles).map(style -> {
            int[] vd = voiceData.get(speaker.name);
            if (vd != null) {
                return new Voice(japan, speaker.name + "(" + style.name + ")", vd[0], vd[1], Voice.VARIANT_DONT_CARE);
            } else {
                return new Voice(japan, speaker.name + "(" + style.name + ")", Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE);
            }
        })).toArray(Voice[]::new);
    }

    /** */
    public int getId(Voice voice) {
        String name = voice.getName().replaceFirst("\\(.+\\)", "");
        Speaker speaker = Arrays.stream(speakers).filter(s -> s.name.equals(name)).findFirst().get();
        String style = voice.getName().substring(voice.getName().indexOf("(") + 1, voice.getName().length() - 1);
        return Arrays.stream(speaker.styles).filter(s -> s.name.equals(style)).findFirst().get().id;
    }

    /** to complement lack information of voicevox for jsapi voice */
    private static final Map<String, int[]> voiceData = new HashMap<>();

    /* cvs: name, gender, age */
    static {
        Scanner scanner = new Scanner(VoiceVox.class.getResourceAsStream("voicevox.csv"));
        while (scanner.hasNextLine()) {
            String[] parts = scanner.nextLine().split(",");
            String name = parts[0];
            int gender = Integer.parseInt(parts[1]);
            int age = Integer.parseInt(parts[2]);
            voiceData.put(name, new int[] {gender, age});
        }
    }
}
