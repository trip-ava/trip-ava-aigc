package group.rxcloud.ava.aigc.service.gpt;

import group.rxcloud.ava.aigc.ai.azure.AzureOpenAIService;
import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import group.rxcloud.ava.aigc.service.AIGCServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * GPT 富文本服务
 */
@Service
public class GptRichEmojiService {

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    private static final String GPT_TRAVEL_NOTE_TEMPLATE =
            "请将以下旅行游记加工为html富文本格式，对于地点展示蓝色，对于旅行相关的关键词，展示橙色。同时使用Emoji风格编辑以下段落，该风格以引人入胜的标题、每个段落中包含表情符号和在末尾添加相关标签为特点。请确保保持原文的意思。：\n" + "%s";

    public static void main(String[] args) {
        GptRichEmojiService gptRichEmojiService = new GptRichEmojiService();
        gptRichEmojiService.azureOpenAIService = new AzureOpenAIService();

        String note = "今天早上，我从海拉尔古城酒店出发，开始了一天的旅行。我们驱车前往莫日格勒河，一路上风景真的很美。路边的黄叶和草原让我感到心旷神怡。\n" +
                "\n" +
                "中午，我们到达了莫日格勒河景区。这里的车真的很多，但是一望无际的大草原和散养的牛、羊和蒙古包让我感到非常惊喜。特别是那壮观的河流，真的让人感到震撼。\n" +
                "\n" +
                "在景区内，我们还参观了天下第一曲水，这个名不虚传的景点真的很美。观景台上的景色也非常壮观，让我感到心旷神怡。最后，我们还体验了越野，真的是一次非常刺激的体验。\n" +
                "\n" +
                "今天的旅行真的让我感到非常充实和愉快。我收获了美丽的风景和难忘的体验，也更深刻地了解了这片美丽的草原。";
        String res = gptRichEmojiService.generateGptRichTextFromNote(note);
        System.out.println(res);
    }

    /**
     * 生成富文本
     */
    public String generateGptRichTextFromNote(String note) {
        AzureAsk azureAsk = new AzureAsk("user", String.format(GPT_TRAVEL_NOTE_TEMPLATE, note));
        String resultAudioPath;
        try {
            resultAudioPath = azureOpenAIService.askChatGpt(Collections.singletonList(azureAsk));
        } catch (Exception e) {
            throw new AIGCServiceException("[generateGptRichTextFromNote] error", e);
        }
        return resultAudioPath;
    }
}
