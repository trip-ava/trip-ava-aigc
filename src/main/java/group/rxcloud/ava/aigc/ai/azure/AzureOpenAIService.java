package group.rxcloud.ava.aigc.ai.azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import group.rxcloud.ava.aigc.ai.AIGCException;
import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import group.rxcloud.ava.aigc.config.AzureConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class AzureOpenAIService {

    private static ObjectMapper om = new ObjectMapper();
    private static MediaType mediaType = MediaType.parse("application/json");

    /**
     * 使用text同chatgpt进行对话
     */
    public String askChatGpt(List<AzureAsk> requestMessage) throws Exception {
        ArrayNode arrayNode = om.createArrayNode();
        requestMessage.stream()
                .forEach(item -> arrayNode.add(getObjectNode(item)));

        ObjectNode dataNode = om.createObjectNode();
        dataNode.putArray("messages")
                .addAll(arrayNode);

        try {
            OkHttpClient client = new OkHttpClient()
                    .newBuilder()
                    .readTimeout(Duration.ofSeconds(30))
                    .build();
            RequestBody body = RequestBody.create(mediaType, dataNode.toString());
            Request request = new Request.Builder()
                    .url(AzureConfig.chatgptUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("api-type", "azure_ad")
                    .addHeader("Authorization", "Bearer " + AzureConfig.azureToken).
                    build();
            Response response = client.newCall(request)
                    .execute();
            JsonNode responseNode = om.readTree(response.body().string());
            String responseContent = responseNode
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();
            return responseContent;
        } catch (IOException e) {
            throw new AIGCException(e);
        }
    }

    private static ObjectNode getObjectNode(AzureAsk azureAsk) {
        if (azureAsk == null) {
            return null;
        }
        ObjectNode node = om.createObjectNode();
        node.put("role", azureAsk.getRole());
        node.put("content", azureAsk.getContent());
        return node;
    }
}
