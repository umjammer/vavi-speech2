/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.docomo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import vavi.speech.docomo.DocomoAIAgentAPI.DialogueRequest;
import vavi.speech.docomo.DocomoAIAgentAPI.DialogueResponse;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;


/**
 * DocomoAIAgentAPITest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-02-18 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@Disabled("end of service")
public class DocomoAIAgentAPITest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "docomo.aiagentapi.apiKey")
    String apiKey;

    @BeforeEach
    void setUp() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("docomo.aiagentapi.apiKey", apiKey);
    }

    @Test
    void test1() throws Exception {
        try (DocomoAIAgentAPI client = new DocomoAIAgentAPI()) {

            DialogueRequest req = new DialogueRequest();
            req.botId = "...";
            req.appUserId = "...";
            req.voiceText = "今日の天気";

            DialogueResponse res = client.dialogue(req);

            System.out.println(res.systemText.expression);
        }
    }
}
