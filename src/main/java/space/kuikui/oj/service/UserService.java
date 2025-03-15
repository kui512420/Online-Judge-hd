package space.kuikui.oj.service;

import space.kuikui.oj.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 30767
* @description 针对表【user】的数据库操作Service
* @createDate 2025-03-15 16:11:44
*/
public interface UserService extends IService<User> {
    String userRegister(String userAccount, String userPassword,String userCheakPassword,String email,String emailCode);

    String sendEmail(String email);
}
