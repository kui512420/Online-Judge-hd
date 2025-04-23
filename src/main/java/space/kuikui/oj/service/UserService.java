package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import space.kuikui.oj.model.dto.UserInfoRequest;
import space.kuikui.oj.model.dto.UserListRequest;
import space.kuikui.oj.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
* @author 30767
* @description 针对表【user】的数据库操作Service
* @createDate 2025-03-15 16:11:44
*/
public interface UserService extends IService<User> {


    Page<User> userList(UserListRequest userListRequest);
    Map<String,String> userLogin(String user, String userPassword, String code, HttpServletRequest request);
    Map<String,String> userRegister(String userAccount, String userPassword, String userCheakPassword, String email, String emailCode, HttpServletRequest request);
    String sendEmail(String email);
    User userInfo(long id);
    int updateUserName(long id, String userName);
    int updateUserProfile(long id, String userProfile);
    int updateUserPassword(long id, String userPassword,String newUserPassword,String email,String code);
    int updateUserAvatar(long id, String userAvatar);
    int logicalDelete(long id);
    Integer putUserRole(Long id, String userRole);

    List<User> queryUsers();

    Integer updateInfo(UserInfoRequest userInfoRequest);
}
