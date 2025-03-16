package space.kuikui.oj.service;

import space.kuikui.oj.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
* @author 30767
* @description 针对表【user】的数据库操作Service
* @createDate 2025-03-15 16:11:44
*/
public interface UserService extends IService<User> {
    Map<String,String> userLogin(String user, String userPassword,String code, HttpServletRequest request);
    Map<String,String> userRegister(String userAccount, String userPassword, String userCheakPassword, String email, String emailCode, HttpServletRequest request);
    String sendEmail(String email);
}
