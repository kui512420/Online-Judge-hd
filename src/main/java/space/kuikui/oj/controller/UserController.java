package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.*;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.service.UserService;
import space.kuikui.oj.utils.CaptchaUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kuikui
 * @date 2025/3/15 16:14
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private CaptchaUtil captchaUtil;
    @Resource
    private JwtLoginUtils jwtLoginUtils;

    /**
     * @todo 用户注册
     * @param userRegisterRequest
     * @param request
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Map<String,String>> register(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String userCheakPassword = userRegisterRequest.getUserCheakPassword();
        String email = userRegisterRequest.getEmail();
        String emailCode = userRegisterRequest.getEmailCode();
        Map<String,String> result = userService.userRegister(userAccount,userPassword,userCheakPassword,email,emailCode,request);
        return ResultUtils.success("注册成功",result);
    }

    /**
     * @todo 用户登录
     * @param userLoginRequset
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<Map<String,String>> register(@RequestBody UserLoginRequset userLoginRequset, HttpServletRequest request) {
        String user = userLoginRequset.getUser();
        String userPassword = userLoginRequset.getPassword();
        String code = userLoginRequset.getCode();

        Map<String,String> result = userService.userLogin(user,userPassword,code,request);
        return ResultUtils.success("登录成功",result);
    }

    /**
     * @todo 获取登录信息
     * @param accessToken
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<Map<Object, Object>> getLogin(@RequestHeader(value = "Accesstoken",required = false) String accessToken) {
        Map<Object, Object> map = new HashMap<>();
        try{
            map = jwtLoginUtils.jwtPeAccess(accessToken);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录") ;
        }
        return ResultUtils.success("获取信息成功",map);
    }
    @GetMapping("/refreshToken")
    public BaseResponse<Map<Object, Object>> refreshToken(@RequestHeader(value = "RefreshToken",required = false) String refreshToken) {
        Map<Object, Object> map = new HashMap<>();
        try{
            long id = (long) jwtLoginUtils.jwtPeRefresh(refreshToken).get("id");
            User user = userService.userInfo(id);
            String accesstoken = jwtLoginUtils.jwtBdAccess(user);
            map.put("accesstoken",accesstoken);
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.LOGIN_TIMEOUT,"登录过期") ;
        }
        return ResultUtils.success("刷新token成功",map);
    }

    /**
     * @todo 发送邮箱
     * @param emailRequest
     * @return
     */
    @PostMapping("/email")
    public BaseResponse<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        String result = userService.sendEmail(emailRequest.getEmail());
        return ResultUtils.success("发送成功",result);
    }

    /**
     * @todo 显示验证码
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        captchaUtil.outCodeImg(request, response);
    }

    /**
     * @todo 修改名称
     * @param accessToken
     * @return
     */
    @PutMapping("/userName")
    public BaseResponse<String> setUerName(@RequestHeader(value = "Accesstoken",required = false) String accessToken,@RequestBody String userName) {
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(accessToken);
        long id = (long) map.get("id");
        int count = userService.updateUserName(id,userName);
        return ResultUtils.success("修改成功",count+"");
    }
    /**
     * @todo 修改个人简介
     * @param accessToken
     * @return
     */
    @PutMapping("/userProfile")
    public BaseResponse<String> setUserProfile(@RequestHeader(value = "Accesstoken",required = false) String accessToken,@RequestBody String userProfile) {
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(accessToken);
        long id = (long) map.get("id");
        int count = userService.updateUserProfile(id,userProfile);
        return ResultUtils.success("修改成功",count+"");
    }

    /**
     * @todo 修改密码
     * @param accessToken
     * @param userPasswordRequest 包含原密码和新密码
     * @return
     */
    @PutMapping("/userPassword")
    public BaseResponse<String> setUserPassword(@RequestHeader(value = "Accesstoken" ,required = false) String accessToken, @RequestBody UserPasswordRequest userPasswordRequest) {
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(accessToken);
        long id = (long) map.get("id");
        int count = userService.updateUserPassword(id,userPasswordRequest.getUsrePassword(),userPasswordRequest.getNewUserPassword(),userPasswordRequest.getEmail(),userPasswordRequest.getCode());
        return ResultUtils.success("修改成功",count+"");
    }
    @GetMapping("/userList")
    public BaseResponse<Page<User>> getUserList(@ModelAttribute UserListRequest userListRequest) {
        Page<User> userList = userService.userList(userListRequest);
        return ResultUtils.success("查询成功",userList);
    }
}
