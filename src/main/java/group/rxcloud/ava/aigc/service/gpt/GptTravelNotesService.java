package group.rxcloud.ava.aigc.service.gpt;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * GPT Travel Notes Service stub
 */
@Service
public class GptTravelNotesService {

    /**
     * Generate travel notes from keywords with timeline style
     *
     * @param keywords keywords list
     * @param style    style description
     * @return generated content
     */
    public String generateGptFromKeywordsWithTimelineStyle(List<List<String>> keywords, String style) {
        // Stub implementation
        return "Generated travel notes with style: " + style;
    }
}
