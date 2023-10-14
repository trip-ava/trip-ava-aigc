package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.LocalDateTime;

@Getter
@Setter
public final class ImageSingleNoteInfo extends AbstractSingleNoteInfo {
    private File image;

    public ImageSingleNoteInfo(File image) {
        super(LocalDateTime.now(), SingleNoteInfoType.IMAGE);
        this.image = image;
    }
}