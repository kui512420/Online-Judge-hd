package space.kuikui.oj.common;

/**
 * @author kuikui
 * @date 2025/3/15 18:00
 *状态码
 */
public enum ErrorCode {

    SUCCESS(200, "Success"),
    NOT_LOGIN_ERROR(40000,"未登录"),
    PARMS_ERROR(40100, "请求参数错误"),
    NOT_AUTH_ERROR(40200,"无权限"),
    FORBINDDEN_ERROR(40300,"账号已过期，请重新登录"),
    LOGIN_TIMEOUT(40400,"登录过期"),
    SYSTEM_ERROR(50000,"系统异常"),
    OPERATION_ERROR(50100,"操作失败"),
    API_REQUEST_ERROR(50200,"接口调用失败");

    /**
     * 状态码
     */
    private final int code;
    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
