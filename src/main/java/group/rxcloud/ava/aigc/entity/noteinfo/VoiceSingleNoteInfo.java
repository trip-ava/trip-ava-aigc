package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
public final class VoiceSingleNoteInfo extends AbstractSingleNoteInfo {
    private File voice;
    private AwsTranscribeVoiceService.Transcript transcript;

    public VoiceSingleNoteInfo(File voice, Position position) {
        super(SingleNoteInfoType.VOICE, position);
        this.voice = voice;
    }
}
