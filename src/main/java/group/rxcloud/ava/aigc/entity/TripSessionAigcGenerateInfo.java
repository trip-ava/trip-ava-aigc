package group.rxcloud.ava.aigc.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TripSessionAigcGenerateInfo {
    private String userId;
    private String tripSessionId;
    private String aigcText;
    private List<File> imageList;
    private int likeCount;
    private int nonLikeCount;
    private LocalDateTime createDateTime = LocalDateTime.now();
}
