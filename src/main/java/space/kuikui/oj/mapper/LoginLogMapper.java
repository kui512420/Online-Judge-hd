package space.kuikui.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import space.kuikui.oj.model.entity.LoginLog;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
    /**
     * 根据用户ID查询最近登录日志
     * @param userId 用户ID
     * @return 登录日志记录
     */
    LoginLog selectLastLoginByUserId(Long userId);
}