package group.rxcloud.ava.aigc.entity.noteinfo;

import com.amazonaws.services.rekognition.model.Label;
import group.rxcloud.ava.aigc.entity.Position;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

/**
 * Image note info
 */
@Getter
@Setter
public class ImageSingleNoteInfo extends AbstractSingleNoteInfo {

    private File file;
    private List<Label> labels;

    public ImageSingleNoteInfo(File file, Position position) {
        super(position);
        this.file = file;
    }
}
