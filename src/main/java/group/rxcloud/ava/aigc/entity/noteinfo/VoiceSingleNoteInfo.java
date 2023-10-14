package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public final class VoiceSingleNoteInfo extends AbstractSingleNoteInfo {
    private File voice;
    private AwsTranscribeVoiceService.Transcript transcript;

    public VoiceSingleNoteInfo(File voice) {
        super(LocalDateTime.now(), SingleNoteInfoType.VOICE);
        this.voice = voice;
    }
}
