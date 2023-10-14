package group.rxcloud.ava.aigc.entity;

import com.amazonaws.services.rekognition.model.Label;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.entity.noteinfo.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class TripRecordInfo {
    private String userId;
    private String tripSessionId;
    private List<AbstractSingleNoteInfo> singleNoteInfoList;

    public List<List<String>> generate() {
        if (CollectionUtils.isEmpty(singleNoteInfoList)) {
            return new ArrayList<>();
        }
        this.singleNoteInfoList.sort(Comparator.comparing(AbstractSingleNoteInfo::getCreateDateTime));
        return this.singleNoteInfoList.stream().map(getAbstractSingleNoteInfoListFunction()).collect(Collectors.toList());
    }

    private static Function<AbstractSingleNoteInfo, List<String>> getAbstractSingleNoteInfoListFunction() {
        return x -> {
            if (x instanceof TextSingleNoteInfo textSingleNoteInfo) {
                return List.of(textSingleNoteInfo.getText());
            }
            if (x instanceof ImageSingleNoteInfo imageSingleNoteInfo) {
                return imageSingleNoteInfo.getLabels().stream().map(Label::getName).collect(Collectors.toList());
            }
            if (x instanceof VoiceSingleNoteInfo voiceSingleNoteInfo) {
                return voiceSingleNoteInfo.getTranscript().getResults().getTranscripts().stream().map(AwsTranscribeVoiceService.Item::getTranscript).collect(Collectors.toList());
            }
            return new ArrayList<>();
        };
    }
}
