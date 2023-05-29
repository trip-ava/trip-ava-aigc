package group.rxcloud.ava.aigc.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/ava")
public class AvaController {

    // -- ava hello
    /*
     * ava say 'hello content' to user
     */

    @Data
    public static class AvaHelloRequest {
        private String userId;
    }

    @Data
    public static class AvaHelloResponse {
        private String content;
    }

    /**
     * i: base user info
     * p: get user trip info -> llm gen content
     * o: hello content
     */
    @PostMapping(path = "/hello")
    public @ResponseBody AvaHelloResponse avaHelloContent(@RequestBody AvaHelloRequest request) {
        String userId = request.getUserId();

        AvaHelloResponse avaHelloResponse = new AvaHelloResponse();
        avaHelloResponse.setContent("");
        return avaHelloResponse;
    }

    @Data
    public static class AvaHelloVoiceRequest {
        private String userId;
        private String content;
    }

    @Data
    public static class AvaHelloVoiceResponse {
        private String voiceUrl;
    }

    /**
     * i: base user info + content
     * p: content -> voice
     * o: voice url
     */
    @PostMapping(path = "/hello/voice")
    public @ResponseBody AvaHelloVoiceResponse avaHelloContent(@RequestBody AvaHelloVoiceRequest request) {
        String userId = request.getUserId();
        String content = request.getContent();

        return new AvaHelloVoiceResponse();
    }

    // -- ava todo
    /*
     *
     */

}
