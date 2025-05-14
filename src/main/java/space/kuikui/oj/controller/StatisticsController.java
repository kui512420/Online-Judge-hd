package space.kuikui.oj.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.service.StatisticsService;

import java.util.Map;

/**
 * 系统统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取系统统计数据
     * @return 系统统计数据
     */
    @GetMapping("/system")
    public BaseResponse<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> statistics = statisticsService.getSystemStatistics();
        return ResultUtils.success("获取统计数据成功", statistics);
    }
    
    /**
     * 获取用户数量
     * @return 用户数量
     */
    @GetMapping("/user/count")
    public BaseResponse<Long> getUserCount() {
        long count = statisticsService.getUserCount();
        return ResultUtils.success("获取用户数量成功", count);
    }
    
    /**
     * 获取题目数量
     * @return 题目数量
     */
    @GetMapping("/question/count")
    public BaseResponse<Long> getQuestionCount() {
        long count = statisticsService.getQuestionCount();
        return ResultUtils.success("获取题目数量成功", count);
    }
    
    /**
     * 获取提交数量
     * @return 提交数量
     */
    @GetMapping("/submit/count")
    public BaseResponse<Long> getSubmitCount() {
        long count = statisticsService.getSubmitCount();
        return ResultUtils.success("获取提交数量成功", count);
    }
    
    /**
     * 获取日志数量
     * @return 日志数量
     */
    @GetMapping("/log/count")
    public BaseResponse<Long> getLogCount() {
        long count = statisticsService.getLogCount();
        return ResultUtils.success("获取日志数量成功", count);
    }
} 