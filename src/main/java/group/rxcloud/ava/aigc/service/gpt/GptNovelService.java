package group.rxcloud.ava.aigc.service.gpt;

import com.amazonaws.util.StringUtils;
import group.rxcloud.ava.aigc.ai.azure.AzureOpenAIService;
import group.rxcloud.ava.aigc.ai.azure.model.AzureAsk;
import group.rxcloud.ava.aigc.service.AIGCServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * GPT 小说服务
 */
@Service
public class GptNovelService {

    /**
     * 默认主人公名字
     */
    public static final String MINE_NAME = "小程";

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    private static final String GPT_POETRY_TEMPLATE =
            "我希望你能作为一个小说家，主人公叫%s。你要想出有创意的、吸引人的故事，能够长时间吸引读者。你可以选择任何体裁，如幻想、浪漫、历史小说等--但目的是要写出有出色的情节线、引人入胜的人物和意想不到的高潮。请基于以下旅行游记进行生成：\n" +
                    "%s";

    public static void main(String[] args) {
        GptNovelService gptNovelService = new GptNovelService();
        gptNovelService.azureOpenAIService = new AzureOpenAIService();

        String travelNote = "今天早上，我从海拉尔古城酒店出发，开始了一天的旅行。我们驱车前往莫日格勒河，一路上风景真的很美。路边的黄叶和草原让我感到心旷神怡。\n" +
                "\n" +
                "中午，我们到达了莫日格勒河景区。这里的车真的很多，但是一望无际的大草原和散养的牛、羊和蒙古包让我感到非常惊喜。特别是那壮观的河流，真的让人感到震撼。\n" +
                "\n" +
                "在景区内，我们还参观了天下第一曲水，这个名不虚传的景点真的很美。观景台上的景色也非常壮观，让我感到心旷神怡。最后，我们还体验了越野，真的是一次非常刺激的体验。\n" +
                "\n" +
                "今天的旅行真的让我感到非常充实和愉快。我收获了美丽的风景和难忘的体验，也更深刻地了解了这片美丽的草原。";
        String res = gptNovelService.generateGptNovelFromNote("十号", travelNote);
        System.out.println(res);
    }

    /**
     * 根据游记生成小说
     */
    public String generateGptNovelFromNote(String mineName, String travelNote) {
        if (StringUtils.isNullOrEmpty(mineName)) {
            mineName = MINE_NAME;
        }

        AzureAsk azureAsk = new AzureAsk("user", String.format(GPT_POETRY_TEMPLATE, mineName, travelNote));
        String resultAudioPath;
        try {
            resultAudioPath = azureOpenAIService.askChatGpt(Collections.singletonList(azureAsk));
        } catch (Exception e) {
            throw new AIGCServiceException("[generateGptNovelFromNote] error", e);
        }
        return resultAudioPath;
    }
}
