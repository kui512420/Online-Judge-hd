package space.kuikui.oj.judge.codesandbox.model.enums;

/**
 * @author kuikui
 * @date 2025/4/5 18:34
 */
public enum JudgeInfoMessageEnum {

    SUCCESS(200, "判题成功"),
    MEMORY_OVERFLOW_ERROR(50000, "内存溢出"),
    JUDGE_TIMEOUT_ERROR(50100, "判题超时");
    /**
     * 状态码
     */
    private final int code;
    /**
     * 信息
     */
    private final String message;

    JudgeInfoMessageEnum(int code, String message) {
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
