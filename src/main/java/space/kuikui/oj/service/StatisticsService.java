package space.kuikui.oj.service;

import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取系统汇总统计数据
     * 包含用户数量、题目数量、提交数量、日志数量等
     * @return 统计数据Map
     */
    Map<String, Object> getSystemStatistics();
    
    /**
     * 获取用户数量
     * @return 用户数量
     */
    long getUserCount();
    
    /**
     * 获取题目数量
     * @return 题目数量
     */
    long getQuestionCount();
    
    /**
     * 获取提交数量
     * @return 提交数量
     */
    long getSubmitCount();
    
    /**
     * 获取日志数量
     * @return 日志数量
     */
    long getLogCount();
} 