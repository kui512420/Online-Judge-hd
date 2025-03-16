package space.kuikui.oj.mapper;

import lombok.extern.log4j.Log4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
    @Select("select * from user where userAccount=#{userAccount} and userPassword=#{userPassword}")
    User findUserByAccountAndPassowrd(@Param("userAccount")  String userAccount, @Param("userPassword") String userPassword);
    @Select("select * from user where email=#{email} and userPassword=#{userPassword}")
    User findUserByEmailAndPassowrd(@Param("email")  String email, @Param("userPassword") String userPassword);
}




