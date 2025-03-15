package space.kuikui.oj.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ResultUtils;

/**
 * @author kuikui
 * @date 2025/3/15 18:28
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return ResultUtils.error(e.getCode(),e.getMessage(),null);
    }
}
