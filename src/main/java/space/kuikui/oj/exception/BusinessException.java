package space.kuikui.oj.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import space.kuikui.oj.common.ErrorCode;

/**
 * @author kuikui
 * @date 2025/3/15 17:58
 * 业务异常处理
 */
public class BusinessException extends RuntimeException {
    /**
     * 状态码 code
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
    public int getCode() {
        return code;
    }
}
