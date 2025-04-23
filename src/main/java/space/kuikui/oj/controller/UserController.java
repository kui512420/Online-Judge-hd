package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import space.kuikui.oj.utils.ExportUtil;

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
    @Resource
    private ExportUtil exportUtil;

    /**
     * 导出文件
     * @param response
     * @throws IOException
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        try{
            List<User> users1 = userService.queryUsers();
            exportUtil.export(response,"xxx.xls",users1,User.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



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
    public BaseResponse<Map<String,String>> login(@RequestBody UserLoginRequset userLoginRequset, HttpServletRequest request) {
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

    /**
     * 查询用户列表
     * @param userListRequest
     * @return
     */
    @PostMapping("/userList")
    public BaseResponse<Page<User>> getUserList(@RequestBody UserListRequest userListRequest) {
        Page<User> userList = userService.userList(userListRequest);
        return ResultUtils.success("查询成功",userList);
    }

    /**
     * 删除单个用户
     * @param id
     * @return
     */
    @DeleteMapping("/user/{id}")
    public BaseResponse<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户不存在");
        }
        return ResultUtils.success("删除成功",true);
    }
    /**
     * 删除多个用户
     * @param ids 用户 ID 列表
     * @return 删除结果
     */
    @DeleteMapping("/user")
    public BaseResponse<Boolean> deleteUsers(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "用户 ID 列表不能为空");
        }
        boolean result = userService.removeByIds(ids);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "部分或全部用户删除失败");
        }
        return ResultUtils.success("删除成功", true);
    }
    /**
     * 更改用户状态
     * @param id
     * @return
     */
    @PutMapping("/user/{id}/{userRole}")
    public BaseResponse<Integer> putUserRole(@PathVariable Long id, @PathVariable String userRole) {
        Integer result = userService.putUserRole(id,userRole);
        return ResultUtils.success("更改成功",result);
    }
    /**
     * 更改用户信息
     * @param id
     * @return
     */
    @PutMapping("/userInfo")
    public BaseResponse<Integer> putUserRole(@RequestBody UserInfoRequest userInfoRequest) {
        Integer result = userService.updateInfo(userInfoRequest);
        return ResultUtils.success("更改成功",result);
    }
}
