package group.rxcloud.ava.aigc.service.gpt;

import group.rxcloud.ava.aigc.ai.azure.AzureOpenAIService;
import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import group.rxcloud.ava.aigc.service.AIGCServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * GPT 诗歌服务
 */
@Service
public class GptPoetryService {

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    private static final String GPT_POETRY_TEMPLATE =
            "我希望你能作为一个诗人。你要创作出能唤起人们情感并有力量搅动人们灵魂的诗篇。写任何话题或主题，但要确保你的文字以美丽而有意义的方式传达你所要表达的感觉。你也可以想出一些短小的诗句，但仍有足够的力量在读者心中留下印记。请基于以下我的旅行游记进行创作：\n" +
                    "%s";

    public static void main(String[] args) {
        GptPoetryService gptPoetryService = new GptPoetryService();
        gptPoetryService.azureOpenAIService = new AzureOpenAIService();

        String travelNote = "今天早上，我从海拉尔古城酒店出发，开始了一天的旅行。我们驱车前往莫日格勒河，一路上风景真的很美。路边的黄叶和草原让我感到心旷神怡。\n" +
                "\n" +
                "中午，我们到达了莫日格勒河景区。这里的车真的很多，但是一望无际的大草原和散养的牛、羊和蒙古包让我感到非常惊喜。特别是那壮观的河流，真的让人感到震撼。\n" +
                "\n" +
                "在景区内，我们还参观了天下第一曲水，这个名不虚传的景点真的很美。观景台上的景色也非常壮观，让我感到心旷神怡。最后，我们还体验了越野，真的是一次非常刺激的体验。\n" +
                "\n" +
                "今天的旅行真的让我感到非常充实和愉快。我收获了美丽的风景和难忘的体验，也更深刻地了解了这片美丽的草原。";
        String res = gptPoetryService.generateGptPoetryFromNote(travelNote);
        System.out.println(res);
    }

    /**
     * 生成诗歌, 从游记中提取关键词
     */
    public String generateGptPoetryFromNote(String travelNote) {
        AzureAsk azureAsk = new AzureAsk("user", String.format(GPT_POETRY_TEMPLATE, travelNote));
        String resultAudioPath;
        try {
            resultAudioPath = azureOpenAIService.askChatGpt(Collections.singletonList(azureAsk));
        } catch (Exception e) {
            throw new AIGCServiceException("[generateGptPoetryFromNote] error", e);
        }
        return resultAudioPath;
    }
}
