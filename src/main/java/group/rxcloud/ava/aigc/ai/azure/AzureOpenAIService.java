package group.rxcloud.ava.aigc.ai.azure;

import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Azure OpenAI Service stub
 */
@Service
public class AzureOpenAIService {

    /**
     * Ask ChatGPT with the given prompts
     *
     * @param asks list of asks
     * @return response string
     */
    public String askChatGpt(List<AzureAsk> asks) {
        // Stub implementation - returns a default response
        return "music/ambient_travel.mp3";
    }
}
