package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.LoginLogRequest;
import space.kuikui.oj.model.dto.SubmitListRequest;
import space.kuikui.oj.model.entity.LoginLog;
import space.kuikui.oj.model.entity.QuestionSubmit;
import space.kuikui.oj.service.LoginLogService;

/**
 * @author kuikui
 * @date 2025/4/27 16:46
 */
@RestController
@RequestMapping("/api/loginLog")
public class LoginLogController {

    @Resource
    private LoginLogService loginLogService;

    /**
     * 查询登录日志
     * @param request 查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public BaseResponse<Page<LoginLog>> getLoginLogList(@RequestBody LoginLogRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR,"参数异常");
        }

        Page<LoginLog> loginLogPage = loginLogService.queryLoginLogs(
                request.getUserAccount(),
                request.getIp(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCurrent(),
                request.getPageSize()
        );

        return ResultUtils.success("获取日志成功",loginLogPage);
    }
}
