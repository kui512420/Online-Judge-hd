package space.kuikui.oj.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.EmailRequest;
import space.kuikui.oj.model.dto.UserRegisterRequest;
import space.kuikui.oj.service.UserService;

import javax.annotation.Resource;

/**
 * @author kuikui
 * @date 2025/3/15 16:14
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<String> login(@RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String userCheakPassword = userRegisterRequest.getUserCheakPassword();
        String email = userRegisterRequest.getEmail();
        String emailCode = userRegisterRequest.getEmailCode();
        String result = userService.userRegister(userAccount,userPassword,userCheakPassword,email,emailCode);
        return ResultUtils.success("注册成功",result);
    }

    @PostMapping("/email")
    public BaseResponse<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        String result = userService.sendEmail(emailRequest.getEmail());
        return ResultUtils.success("发送成功",result);
    }
}
