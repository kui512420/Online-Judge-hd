package space.kuikui.oj.mapper;

import org.apache.ibatis.annotations.Mapper;
import space.kuikui.oj.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 30767
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-03-15 16:11:44
* @Entity space.kuikui.oj.model.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
    int insertUser(User user);
}




