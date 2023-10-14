package group.rxcloud.ava.aigc.service.gpt;

import group.rxcloud.ava.aigc.ai.azure.AzureOpenAIService;
import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import group.rxcloud.ava.aigc.service.AIGCServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GPT 旅行游记服务
 */
@Service
public class GptTravelNotesService {

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    private static final String GPT_TRAVEL_NOTE_TEMPLATE =
            "你作为一名旅行游记博主，现在我会将我一天中的行程相关的信息输入给你，我将会按照时间线的顺序发给你，代表了我一天中按时间去过的地方和我的旅行感受，其中有些内容是通过语音翻译出来的，可能有些失真，你需要结合上下文游记对其进行纠正。你的任务是基于我提供的文本和关键词，改进所提供文本的拼写、语法、清晰、简洁和整体可读性，同时生成一篇包含了以上内容的游记，游记要求符合旅行之后的游记规范，可以参考小红书博主发表的旅行游记，同时要求文本内容为html富文本，对地点展示蓝色，对关键词展示橙色，同时使用Emoji风格编辑，该风格以引人入胜的标题、每个段落中包含表情符号和在末尾添加相关标签为特点。请务必保证以上要求，以下我将给出今天我的行程信息：\n" +
                    "%s";

    public static void main(String[] args) {
        GptTravelNotesService gptTravelNotesService = new GptTravelNotesService();
        gptTravelNotesService.azureOpenAIService = new AzureOpenAIService();

        List<List<String>> keywords = new ArrayList<>();
        keywords.add(Arrays.asList("早上", "我从海拉尔古城酒店启程"));
        keywords.add(Arrays.asList("开车去莫日格勒河咯", "路上真好看"));
        keywords.add(Arrays.asList("黄叶", "草原"));
        keywords.add(Arrays.asList("到达莫日格勒河景区"));
        keywords.add(Arrays.asList("车好多"));
        keywords.add(Arrays.asList("一望无际的大草原"));
        keywords.add(Arrays.asList("牛", "羊", "蒙古包"));
        keywords.add(Arrays.asList("好壮观的河", "天下第一曲水名不虚传"));
        keywords.add(Arrays.asList("观景台真壮观", "越野好好玩"));
        String res = gptTravelNotesService.generateGptFromKeywordsWithTimeline(keywords);
        System.out.println(res);
    }

    /**
     * 生成游记, 带时间线
     */
    public String generateGptFromKeywordsWithTimeline(List<List<String>> keywords) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= keywords.size(); i++) {
            List<String> keyword = keywords.get(i - 1);
            stringBuilder.append(i)
                    .append(".")
                    .append(keyword.stream().collect(Collectors.joining(",")))
                    .append(" ");
        }

        AzureAsk azureAsk = new AzureAsk("user", String.format(GPT_TRAVEL_NOTE_TEMPLATE, stringBuilder.toString()));
        String resultAudioPath;
        try {
            resultAudioPath = azureOpenAIService.askChatGpt(Collections.singletonList(azureAsk));
        } catch (Exception e) {
            throw new AIGCServiceException("[generateGptFromKeywordsWithTimeline] error", e);
        }
        return resultAudioPath;
    }
}
