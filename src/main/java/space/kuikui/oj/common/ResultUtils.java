package space.kuikui.oj.common;

/**
 * @author kuikui
 * @date 2025/3/15 16:24
 */
public class ResultUtils {
    public static <T> BaseResponse<T> success(String message,T data) {
        return new BaseResponse<>(200, message, data);
    }
    public static <T> BaseResponse<T> error(Integer code,String message,T data) {
        return new BaseResponse<>(code, message, data);
    }
}
