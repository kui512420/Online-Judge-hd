package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import space.kuikui.oj.mapper.LoginLogMapper;
import space.kuikui.oj.mapper.QuestionMapper;
import space.kuikui.oj.mapper.QuestionSubmitMapper;
import space.kuikui.oj.mapper.UserMapper;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.QuestionSubmit;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.service.StatisticsService;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private QuestionMapper questionMapper;
    
    @Resource
    private QuestionSubmitMapper questionSubmitMapper;
    
    @Resource
    private LoginLogMapper loginLogMapper;

    @Override
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 收集各类统计数据
        result.put("userCount", getUserCount());
        result.put("questionCount", getQuestionCount());
        result.put("submitCount", getSubmitCount());
        result.put("logCount", getLogCount());
        
        return result;
    }

    @Override
    public long getUserCount() {
        // 查询未删除的用户数量
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        return userMapper.selectCount(queryWrapper);
    }

    @Override
    public long getQuestionCount() {
        // 查询题目数量（可以按需添加条件，如只查询未删除的题目）
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        return questionMapper.selectCount(queryWrapper);
    }

    @Override
    public long getSubmitCount() {
        // 查询提交数量
        return questionSubmitMapper.selectCount(null);
    }

    @Override
    public long getLogCount() {
        // 查询日志数量
        return loginLogMapper.selectCount(null);
    }
} 