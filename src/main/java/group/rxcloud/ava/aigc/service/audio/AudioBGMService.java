package group.rxcloud.ava.aigc.service.audio;

import group.rxcloud.ava.aigc.ai.azure.AzureOpenAIService;
import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import group.rxcloud.ava.aigc.service.AIGCServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AudioBGMService {

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    private static final String CHOOSE_AUDIO_TEMPLATE =
            "只回答音乐文件名，必须从中选择一首。我有以下关键词：%s，" +
                    "请帮我选一首和关键词相关的、适合做旅游vlog的音乐。";

    public static void main(String[] args) {
        AudioBGMService audioBGMService = new AudioBGMService();
        audioBGMService.azureOpenAIService = new AzureOpenAIService();

        List<String> keywords = Arrays.asList("黄山", "彩虹");
        String res = audioBGMService.chooseBGMAudio(keywords);
        System.out.println(res);
    }

    public String chooseBGMAudio(List<String> keywords) {
        AzureAsk azureAsk = new AzureAsk("user", String.format(CHOOSE_AUDIO_TEMPLATE, keywords));
        String resultAudioPath;
        try {
            resultAudioPath = azureOpenAIService.askChatGpt(Collections.singletonList(azureAsk));
        } catch (Exception e) {
            throw new AIGCServiceException("[chooseBGMAudio] error", e);
        }
        return resultAudioPath;
    }
}
