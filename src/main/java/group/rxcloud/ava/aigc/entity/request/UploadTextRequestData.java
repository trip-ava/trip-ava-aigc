package group.rxcloud.ava.aigc.entity.request;

import com.aliyun.credentials.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadTextRequestData {
    private String text;
    private String longitude;
    private String latitude;
    public boolean isIllegal() {
        if (stringIsIllegal(text)
                || stringIsIllegal(longitude)
                || stringIsIllegal(latitude)) {
            return true;
        }
        return false;
    }

    private static boolean stringIsIllegal(String str) {
        return str == null ||
                StringUtils.isEmpty(str.trim());
    }
}
