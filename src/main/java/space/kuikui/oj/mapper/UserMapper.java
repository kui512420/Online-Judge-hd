package space.kuikui.oj.mapper;

import lombok.extern.log4j.Log4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import space.kuikui.oj.model.dto.UserInfoRequest;
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
    @Select("select * from user where id=#{id}")
    User findUserById(@Param("id")  long id);
    @Select("select userPassword from user where id=#{id}")
    String findUsePasswordById(@Param("id")  long id);
    @Update("update user set userName=#{userName} where id=#{id}")
    int updateUserName(@Param("id")  long id,@Param("userName")  String userName);
    @Update("update user set userProfile=#{userProfile} where id=#{id}")
    int updateUserProfile(@Param("id")  long id,@Param("userProfile")  String userProfile);
    @Update("update user set userPassword=#{userPassword} where id=#{id}")
    int updateUserPassword(@Param("id")  long id,@Param("userPassword")  String userPassword);
    @Update("update user set userAvatar=#{userAvatar} where id=#{id}")
    int updateUserAvatar(@Param("id")  long id,@Param("userAvatar")  String userAvatar);
    @Update("update user set isDelete=1 where id=#{id}")
    int logicalDelete(@Param("id")  long id);
    @Update("update user set userRole=#{userRole} where id=#{id}")
    int updateUserRole(@Param("id") Long id,@Param("userRole") String userRole);
    @Update("UPDATE user " +
            "SET userName = #{userName}, " +
            "userPassword = #{password}, " +
            "email = #{email}, " +
            "userRole = #{userRole} " +
            "WHERE id = #{id}")
    Integer updateInfo(UserInfoRequest userInfoRequest);
}




