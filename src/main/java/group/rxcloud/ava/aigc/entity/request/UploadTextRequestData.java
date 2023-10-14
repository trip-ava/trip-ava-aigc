package group.rxcloud.ava.aigc.entity.request;

import com.aliyun.credentials.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadTextRequestData {
    private String text;

    public boolean isIllegal() {
        if (text == null ||
                StringUtils.isEmpty(text.trim())) {
            return true;
        }
        return false;
    }
}
