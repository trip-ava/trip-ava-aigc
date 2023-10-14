package group.rxcloud.ava.aigc.entity.noteinfo;

import com.amazonaws.services.rekognition.model.Label;
import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
public final class ImageSingleNoteInfo extends AbstractSingleNoteInfo {
    private File image;
    private List<Label> labels;

    public ImageSingleNoteInfo(File image, Position position) {
        super(SingleNoteInfoType.IMAGE, position);
        this.image = image;
    }
}