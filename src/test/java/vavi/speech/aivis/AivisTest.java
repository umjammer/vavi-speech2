/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aivis;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.speech.aivis.Aivis.AivisSpeaker;
import vavi.speech.aivis.Aivis.AudioQuery;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static vavi.sound.SoundUtil.volume;


/**
 * AivisTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-12-14 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class AivisTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    /** */
    void speak(InputStream is) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
        DataLine.Info line = new DataLine.Info(Clip.class, ais.getFormat());
        Clip clip = (Clip) AudioSystem.getLine(line);
        CountDownLatch cdl = new CountDownLatch(1);
        clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) cdl.countDown(); });
        clip.open(ais);
        volume(clip, volume);
        clip.start();
        cdl.await();
        clip.stop();
        clip.close();
    }

    @Test
    @DisplayName("use wrapped api")
    void test3() throws Exception {
        int speakerId = 888753760; // Anneli(ノーマル)
        Aivis aivis = new Aivis();
        AudioQuery audioQuery = aivis.getQuery("ひざまずくが良いのだ、この愚かな地球人共よ", speakerId);
        audioQuery.setSpeed(1.1f);
        audioQuery.setVolume(0.1f);
        speak(aivis.synthesize(audioQuery, speakerId));
        aivis.close();
    }

    @Test
    @DisplayName("wrapped api list voices")
    void test4() throws Exception {
        Aivis aivis = new Aivis();
        AivisSpeaker[] voices = aivis.getAllVoices();
        Arrays.stream(voices).forEach(System.err::println);
        assertEquals(888753760, voices[0].id);
        aivis.close();
    }
}
