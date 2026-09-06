/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.docomo;

import java.io.Closeable;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.spi.ErrorDetail;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static java.lang.System.getLogger;


/**
 * DocomoAIAgentAPI.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-14 nsano initial version <br>
 * @deprecated end of service
 */
@Deprecated
public class DocomoAIAgentAPI implements Closeable {

    private static final Logger logger = getLogger(DocomoAIAgentAPI.class.getName());

    /** */
    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final String apiKey = System.getProperty("docomo.aiagentapi.apiKey");

    /** */
    private final WebTarget textGw;
    private final WebTarget udsBackend;

    /** */
    private final Client client;

    /** */
    public DocomoAIAgentAPI() {
        client = ClientBuilder.newClient(); // DON'T CLOSE
        textGw = client.target("https://txtgw.aiplat.jp/v1.0/dvo");
        udsBackend = client.target("https://doubk.aiplat.jp/v1.0/dvo");
        if (apiKey == null) {
            throw new IllegalStateException("the system property 'docomo.aiagentapi.apiKey' is not set.");
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    protected <T> T post(WebTarget target, String path, Object request, Class<T> responseType) {
        String json = gson.toJson(request);

        try (Response res = client
                .target(target.getUri())
                .path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("x-api-key", apiKey)
                .post(Entity.json(json))) {

            if (res.getStatus() >= 300) {
                throw new RuntimeException("HTTP " + res.getStatus() + ": " + res.readEntity(String.class));
            }
            return gson.fromJson(res.readEntity(String.class), responseType);
        }
    }

    /** */
    public static class DialogueRequest {
        public String botId;
        public String appUserId;
        public String voiceText;
        public String clientVer = "1.0";
        public String language = "ja-JP";
        public Boolean initTalkingFlag = false;
        public Map<String, Object> clientData;
    }

    /** */
    public static class DialogueResponse {
        public SystemText systemText;
        public String serverSendTime;
        public String command;
        public GenericResult result;
    }

    /** */
    public static class SystemText {
        public String expression;
        public String utterance;
    }

    /** */
    public static class GenericResult {
        public String code;
        public String message;
        public List<ErrorDetail> details;
    }

    static class WebUserRegistrationResponse extends GenericResult {}
    static class DeviceIdResponse extends GenericResult {}
    static class DeviceTokenResponse extends GenericResult {}

    static class WebUserRegistrationRequest extends DialogueRequest {}
    static class DeviceIdRequest extends DialogueRequest {}
    static class DeviceTokenRequest extends DialogueRequest {}

    /** */
    public DialogueResponse dialogue(DialogueRequest req) {
        return post(textGw, "/dotac/dialogue", req, DialogueResponse.class);
    }

    public WebUserRegistrationResponse registerWebUser(WebUserRegistrationRequest req) {
        return post(textGw, "/dotac/registration", req, WebUserRegistrationResponse.class);
    }

    public DeviceIdResponse createDeviceId(DeviceIdRequest req) {
        return post(udsBackend, "/doubk/devices", req, DeviceIdResponse.class);
    }

    public DeviceTokenResponse createDeviceToken(DeviceTokenRequest req) {
        return post(udsBackend, "/doubk/devices/token", req, DeviceTokenResponse.class);
    }

    /** */
    public Voice[] getAllVoices() {
        //
        SpeechLocale japan = new SpeechLocale(Locale.JAPANESE.toString());
        return new Voice[] {new Voice(japan, "default", Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, Voice.VARIANT_DEFAULT)};
    }

    /** to complement lack information of voicevox for jsapi voice */
    private static final Map<String, int[]> voiceData = new HashMap<>();
}
