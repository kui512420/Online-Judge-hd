package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import space.kuikui.oj.model.entity.LoginLog;

import java.util.Date;

/**
 * @author kuikui
 * @date 2025/4/25 9:57
 */

public interface LoginLogService {
    public int addLoginLog(LoginLog loginLog);
    /**
     * 组合查询登录日志
     * @param userAccount 用户账号
     * @param ip IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    Page<LoginLog> queryLoginLogs(String userAccount, String ip,
                                  Date startTime, Date endTime,
                                  long current, long size);
}
