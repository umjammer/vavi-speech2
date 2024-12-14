/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.coeiroink;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.google.api.client.util.ExponentialBackOff;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
import vavi.util.CharNormalizerJa;


/**
 * CoeiroInk.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-04-02 nsano initial version <br>
 */
public class CoeiroInk implements Closeable {

    private static final Logger logger = System.getLogger(CoeiroInk.class.getName());

    /** rest response parser */
    private static final Gson gson = new GsonBuilder().create();

    /** rest target */
    private final WebTarget target;

    /** rest client */
    private final Client client;

    /** server url */
    private static String getUrl() {
        String url = System.getProperty("vavi.speech.coeiroink.url", null);
        if (url == null || !url.startsWith("http:")) {
            return "http://localhost:50032/";
        } else {
            return url;
        }
    }

    /** */
    public CoeiroInk() {
        this(getUrl());
    }

    /** */
    public CoeiroInk(String url) {
        try {
            client = ClientBuilder.newClient(); // DON'T CLOSE
            target = client.target(url);

            String json = target
                    .path("/v1/engine_info")
                    .request()
                    .get(String.class);
            EngineInfo engineInfo = gson.fromJson(json, EngineInfo.class);
logger.log(Level.DEBUG, "version: " + engineInfo.version);
        } catch (Exception e) {
            throw new IllegalStateException("CoeiroInk is not available at " + url, e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /** */
    public Prosody getProsody(String text) {
        Entity<String> entity = Entity.entity("{ \"text\": \"" + text + "\"}", MediaType.APPLICATION_JSON);
        String json = target
                .path("v1/estimate_prosody")
                .request()
                .post(entity, String.class);
        return gson.fromJson(json, Prosody.class);
    }

    /** */
    public InputStream synthesize(Synthesis synthesis) {
        Entity<String> syntheEntity = Entity.entity(gson.toJson(synthesis), MediaType.APPLICATION_JSON);
        return target.path("v1/synthesis")
                .request()
                .post(syntheEntity, InputStream.class);
    }

    /** */
    public Speaker[] getAllVoices() {
        String speakersJson = target
                .path("/v1/speakers")
                .request()
                .get(String.class);
        return gson.fromJson(speakersJson, Speaker[].class);
    }

    //----

    public static class EngineInfo {
        public String device;
        public String version;
    }

    public static class Speaker {
        public String speakerName;
        public String speakerUuid;
        public static class Style {
            public String styleName;
            public int styleId;
            public String base64Icon;
            public String base64Portrait;
            @Override public String toString() {
                return new StringJoiner(", ", Style.class.getSimpleName() + "[", "]")
                        .add("styleName='" + styleName + "'")
                        .add("styleId=" + styleId)
                        .toString();
            }
        }
        public Style[] styles;
        public String version;
        public String base64Portrait;
        @Override public String toString() {
            return new StringJoiner(", ", Speaker.class.getSimpleName() + "[", "]")
                    .add("speakerName='" + speakerName + "'")
                    .add("speakerUuid='" + speakerUuid + "'")
                    .add("styles=" + Arrays.toString(styles))
                    .add("version='" + version + "'")
                    .toString();
        }
    }

    public static class Detail {
        public String phoneme;
        public String hira;
        public int accent;

        public Detail(String phoneme, String hira, int accent) {
            this.phoneme = phoneme;
            this.hira = hira;
            this.accent = accent;
        }
    }

    public static class Prosody {
        public String[] plain;
        public Detail[][] detail;

        @Override public String toString() {
            return new StringJoiner(", ", Prosody.class.getSimpleName() + "[", "]")
                    .add("plain=" + Arrays.toString(plain))
                    .add("detail=" + Arrays.toString(detail))
                    .toString();
        }
    }

    public static class Mora {
        public String text;
        public String consonant;
        public Integer consonant_length;
        public String vowel;
        public int vowel_length;
        public int pitch;

        public Mora(String text, String consonant, Integer consonant_length, String vowel, int vowel_length, int pitch) {
            this.text = text;
            this.consonant = consonant;
            this.consonant_length = consonant_length;
            this.vowel = vowel;
            this.vowel_length = vowel_length;
            this.pitch = pitch;
        }
    }

    public static class AccentPhrase {
        public Mora[] moras;
        public int accent;
        public boolean is_interrogative;
        public Mora pause_mora;

        public AccentPhrase(Mora[] moras, int accent, boolean is_interrogative, Mora pause_mora) {
            this.moras = moras;
            this.accent = accent;
            this.is_interrogative = is_interrogative;
            this.pause_mora = pause_mora;
        }
    }

    Function<Prosody, AccentPhrase[]> prosodyToAccentPhrases = prosody -> {
        List<AccentPhrase> result = new ArrayList<>();
        for (var d : prosody.detail) {
            int accentPosition = -1;
            List<Mora> _moras = new ArrayList<>();
            int moraIndex = -1;
            for (var m : d) {
                moraIndex++;
                if (m.hira.equals("、")) {
                    result.get(result.size() - 1).pause_mora = new Mora(
                            "、",
                            null,
                            null,
                            "pau",
                            0,
                            0
                    );
                } else {
                    String _vowel, consonant;
                    if (m.phoneme.contains("-")) {
                        String[] pair = m.phoneme.split("-");
                        consonant = pair[0];
                        _vowel = pair[1];
                    } else {
                        consonant = null;
                        _vowel = m.phoneme;
                    }
                    _moras.add(new Mora(
                            CharNormalizerJa.ToKatakana.normalize(m.hira),
                            consonant,
                            consonant != null ? 0 : null,
                            _vowel,
                            0,
                            0
                    ));
                    if (m.accent == 1) {
                        accentPosition = moraIndex;
                    }
                }
            }
            if (_moras.isEmpty()) {
                continue;
            }
            result.add(new AccentPhrase(
                    _moras.toArray(Mora[]::new),
                    accentPosition + 1,
                    false,
                    null
            ));
        }
        return result.toArray(AccentPhrase[]::new);
    };

    Function<AccentPhrase[], Detail[]> accentPhrasesToProsody = accentPhrases -> {
        return Arrays.stream(accentPhrases).map(accentPhrase -> {
            List<Detail> detail = new ArrayList<>();

            int i = 0;
            Arrays.stream(accentPhrase.moras).forEach(mora -> {
                String phoneme;
                if (mora.consonant != null && !mora.consonant.isEmpty()) {
                    phoneme = mora.consonant + "-" + mora.vowel;
                } else {
                    phoneme = mora.vowel;
                }

                var accent = 0;
                if (i == accentPhrase.accent - 1 || (i != 0 && i <= accentPhrase.accent - 1)) {
                    accent = 1;
                }

                detail.add(new Detail(
                        CharNormalizerJa.ToHiragana.normalize(mora.text),
                        phoneme,
                        accent
                ));
            });

            if (accentPhrase.pause_mora != null) {
                detail.add(new Detail(
                    "、",
                    "_",
                    0
                ));
            }

            return detail;
        }).flatMap(Collection::stream).toArray(Detail[]::new);
    };

    public static class Synthesis {
        public String speakerUuid;
        public int styleId;
        public String text;
        public Detail[][] prosodyDetail;
        /** 0.5 ~ 1 ~ 2 */
        public float speedScale = 1;
        /** 0 ~ 1 ~ +2 */
        public float volumeScale = 1;
        /** -0.15 ~ 0 ~ +0.15 */
        public float pitchScale = 0;
        /** 0 ~ 1 ~ +2 */
        public float intonationScale = 1;
        /** 0 ~ 0.1 ~ +1.50 */
        public float prePhonemeLength = 0.1f;
        /** 0 ~ 0.1 ~ +1.50 */
        public float postPhonemeLength = 0.1f;
        public int outputSamplingRate = 22050;
    }

    public static Response execWithBackoff(Callable<Response> i) {
        ExponentialBackOff backoff = new ExponentialBackOff.Builder().build();

        long delay = 0;

        Response response;
        do {
            try {
                Thread.sleep(delay);

                response = i.call();

                if (response.getStatusInfo().getFamily() == Family.SERVER_ERROR) {
                    logger.log(Level.WARNING, String.format("Server error %s when accessing path %s. Delaying %dms", response.getStatus(), response.getLocation() != null ? response.getLocation().toASCIIString() : "null", delay));
                }

                delay = backoff.nextBackOffMillis();
            } catch (Exception e) { //callable throws exception
                throw new RuntimeException("Client request failed", e);
            }

        } while (delay != ExponentialBackOff.STOP && response.getStatusInfo().getFamily() == Family.SERVER_ERROR);

        if (response.getStatusInfo().getFamily() == Family.SERVER_ERROR) {
            throw new IllegalStateException("Client request failed for " + response.getLocation().toASCIIString());
        }

        return response;
    }
}
