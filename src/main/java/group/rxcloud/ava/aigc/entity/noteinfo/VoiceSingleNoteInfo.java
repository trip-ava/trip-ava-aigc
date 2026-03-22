package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.entity.Position;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * Voice note info
 */
@Getter
@Setter
public class VoiceSingleNoteInfo extends AbstractSingleNoteInfo {

    private File file;
    private AwsTranscribeVoiceService.Transcript transcript;

    public VoiceSingleNoteInfo(File file, Position position) {
        super(position);
        this.file = file;
    }
}
