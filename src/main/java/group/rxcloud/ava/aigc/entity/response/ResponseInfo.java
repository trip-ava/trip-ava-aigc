package group.rxcloud.ava.aigc.entity.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseInfo<T> {

    /**
     * 1正常；其他失败
     */
    private int responseCode;

    /**
     * responseCode非1时的异常信息
     */
    private String errorMessage;

    private T data;

    public static <T> ResponseInfo<T> buildSuccess(T data) {
        ResponseInfo<T> responseInfo = new ResponseInfo<>();
        responseInfo.setResponseCode(1);
        responseInfo.setErrorMessage(null);
        responseInfo.setData(data);
        return responseInfo;
    }

    public static  ResponseInfo<?> buildError(String errorMessage) {
        ResponseInfo<?> responseInfo = new ResponseInfo<>();
        responseInfo.setResponseCode(-1);
        responseInfo.setErrorMessage(errorMessage);
        responseInfo.setData(null);
        return responseInfo;
    }
}
