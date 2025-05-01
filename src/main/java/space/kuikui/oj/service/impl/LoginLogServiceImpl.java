package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import space.kuikui.oj.mapper.LoginLogMapper;
import space.kuikui.oj.model.entity.LoginLog;
import space.kuikui.oj.service.LoginLogService;

import java.util.Date;

/**
 * @author kuikui
 * @date 2025/4/25 9:58
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;

    @Override
    public int addLoginLog(LoginLog loginLog) {
        loginLogMapper.insert(loginLog);
        return 0;
    }
    @Override
    public Page<LoginLog> queryLoginLogs(String userAccount, String ip,
                                         Date startTime, Date endTime,
                                         long current, long size) {
        // 创建查询条件
        QueryWrapper<LoginLog> queryWrapper = new QueryWrapper<>();

        // 添加查询条件
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.like("user", userAccount);
        }
        if (StringUtils.isNotBlank(ip)) {
            queryWrapper.like("ip", ip);
        }
        if (startTime != null) {
            queryWrapper.ge("loginTime", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("loginTime", endTime);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("loginTime");

        // 分页查询
        Page<LoginLog> page = new Page<>(current, size);
        return loginLogMapper.selectPage(page, queryWrapper);
    }
}
