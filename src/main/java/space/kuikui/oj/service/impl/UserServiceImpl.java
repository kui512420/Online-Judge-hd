package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.UserListRequest;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.service.UserService;
import space.kuikui.oj.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.DigestUtils;
import space.kuikui.oj.utils.CaptchaEmailUtils;
import space.kuikui.oj.utils.CaptchaUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 30767
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-03-15 16:11:44
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>implements UserService{

    @Resource
    private UserMapper userMapper;
    @Resource
    private JavaMailSender mailSender = new JavaMailSenderImpl();
    @Autowired
    private CaptchaEmailUtils captchaEmailUtils;
    @Resource
    private JwtLoginUtils jwtLoginUtils;
    @Resource
    private CaptchaUtil captchaUtil;

    @Value("${spring.mail.username}")
    private String from;
    private static String SALT = "KUIKUI";
    /**
     *
     * type:
     * 0 全部查询
     * 1 通过id查询
     * 2 通过账号查询
     * 3 通过邮箱查询
     */
    @Override
    public Page<User> userList(UserListRequest userListRequest) {

        // 1. 创建 MyBatis-Plus 分页对象
        Page<User> page = new Page<>(userListRequest.getPage(), userListRequest.getSize());
        // 2. 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        int type = userListRequest.getType();
        // 3. 根据 type 设置不同的查询条件
        switch (type) {
            case 0: // 全查
                queryWrapper.orderByDesc("createTime");
                break;
            case 1: // 按 ID 查询
                queryWrapper.eq("id", userListRequest.getId());
                break;
            case 2: // 按账号查询
                queryWrapper.eq("userAccount", userListRequest.getUserAccount());
                break;
            case 3: // 按邮箱查询
                queryWrapper.eq("email", userListRequest.getEmail());
                break;
            default:
                throw new BusinessException(ErrorCode.PARMS_ERROR, "type值错误");
        }
        // 4. 执行分页查询（MyBatis-Plus 的 selectPage）
        Page<User> resultPage = userMapper.selectPage(page, queryWrapper);

        return resultPage;
    }

    /**
     *
     * @param user
     * @param userPassword
     * @param code
     * @param request
     * @return 双token
     */
    @Override
    public Map<String, String> userLogin(String user, String userPassword, String code, HttpServletRequest request) {
        if(StringUtils.isAnyBlank(user,userPassword,code)){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"参数不能为空");
        }
        String encryptPassword =  DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user1 = userMapper.findUserByAccountAndPassowrd(user, encryptPassword);
        User user2 = userMapper.findUserByEmailAndPassowrd(user, encryptPassword);

        boolean cheakCode = captchaUtil.validateCode(request,code);
        if(!cheakCode){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"图片验证码错误");
        }

        Map<String, String> map = new HashMap<>();
        if(user1==null && user2==null){
            throw new BusinessException(ErrorCode.PARMS_ERROR, "账号或密码错误");
        }else{
            if(user1!=null){
                String accessToken = jwtLoginUtils.jwtBdAccess(user1);
                String RefreshToken = jwtLoginUtils.jwtBdRefresh(user1.getId(),request);
                map.put("accessToken",accessToken);
                map.put("RefreshToken",RefreshToken);
            }else{
                String accessToken = jwtLoginUtils.jwtBdAccess(user2);
                String refreshToken = jwtLoginUtils.jwtBdRefresh(user2.getId(),request);
                map.put("AccessToken",accessToken);
                map.put("RefreshToken",refreshToken);
            }
        }
        return map;
    }

    /**
     *
     * @param userAccount
     * @param userPassword
     * @param userCheakPassword
     * @param email
     * @param emailCode
     * @param request
     * @return 双token
     * @throws BusinessException
     */
    @Override
    public Map<String,String> userRegister(String userAccount, String userPassword, String userCheakPassword, String email, String emailCode, HttpServletRequest request){
            if(StringUtils.isAnyBlank(userAccount,userPassword,userCheakPassword,email,emailCode)){
                throw new BusinessException(ErrorCode.PARMS_ERROR,"参数不能为空");
            }else if(!(userAccount.length()>=6 && userAccount.length()<=15)){
                throw new BusinessException(ErrorCode.PARMS_ERROR,"用户名限制：6~15位");
            }else if(!(userPassword.length()>=6 && userPassword.length()<=15)){
                throw new BusinessException(ErrorCode.PARMS_ERROR,"密码限制：6~15位");
            }else if(!userPassword.equals(userCheakPassword)){
                throw new BusinessException(ErrorCode.PARMS_ERROR,"两次密码输入不同");
            }else if(!captchaEmailUtils.check(email,emailCode)){
                throw new BusinessException(ErrorCode.PARMS_ERROR,"验证码错误或失效");
            }


        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapperUser = new QueryWrapper<>();
            queryWrapperUser.eq("userAccount", userAccount);
            long userAccountCount = this.baseMapper.selectCount(queryWrapperUser);
            if (userAccountCount > 0) {
                throw new BusinessException(ErrorCode.PARMS_ERROR,"账号已经存在");
            }
            QueryWrapper<User> queryWrapperEmail = new QueryWrapper<>();
            queryWrapperEmail.eq("email", email);
            long EmailCount = this.baseMapper.selectCount(queryWrapperEmail);
            if (EmailCount > 0) {
                throw new BusinessException(ErrorCode.PARMS_ERROR,"邮箱已经存在");
            }
            //  加密 + 盐
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setEmail(email);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
            }else{
                String accessToken = jwtLoginUtils.jwtBdAccess(user);
                String refreshToken = jwtLoginUtils.jwtBdRefresh(user.getId(),request);
                Map<String,String> map = new HashMap<>();
                map.put("AccessToken",accessToken);
                map.put("RefreshToken",refreshToken);
                return map;
            }
        }
    }

    /**
     * 发送验证码
     * @param email
     * @return null
     */
    @Override
    public String sendEmail(String email) {
        SimpleMailMessage emailObj = new SimpleMailMessage();
        emailObj.setTo(email);
        emailObj.setSubject("【KUIKUI OJ】注册");
        emailObj.setFrom(from);
        String txt = "【KUIKUI OJ】注册验证码："+captchaEmailUtils.generateCaptcha(email)+"，3分钟内有效。";
        emailObj.setText(txt);
        try {
            mailSender.send(emailObj);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发送验证码失败");
        }
        return null;
    }

    @Override
    public User userInfo(long id) {
        return userMapper.findUserById(id);
    }

    @Override
    public int updateUserName(long id, String userName) {
        userName = userName.replace("\"","");
        int count = 0;
        try{
            count = userMapper.updateUserName(id,userName);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改昵称失败");
        }
        return count;
    }

    @Override
    public int updateUserProfile(long id, String userProfile) {
        userProfile = userProfile.replace("\"","");
        int count = 0;
        try{
            count = userMapper.updateUserProfile(id,userProfile);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改个人简介失败");
        }
        return count;
    }

    @Override
    public int updateUserPassword(long id, String userPassword, String newUserPassword,String email,String code) {
        String savePassword = userMapper.findUsePasswordById(id);
        String inputPassword =  DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        CaptchaEmailUtils captchaEmailUtils = new CaptchaEmailUtils();
        boolean cheakCode = captchaEmailUtils.check(email,code);
        if(!cheakCode){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"邮箱验证码错误");
        }
        int count = 0;
        if(savePassword.equals(inputPassword)){
            try{
                String newPassword = DigestUtils.md5DigestAsHex((SALT + newUserPassword).getBytes());
                count = userMapper.updateUserPassword(id,newPassword);
            }catch (Exception e){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改密码失败");
            }
        }else{
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"原密码错误");
        }
        return count;
    }

    @Override
    public int updateUserAvatar(long id, String userAvatar) {
        return userMapper.updateUserAvatar(id,userAvatar);
    }

    @Override
    public int logicalDelete(long id) {
        return userMapper.logicalDelete(id);
    }

    @Override
    public Integer putUserRole(Long id, String userRole) {
        return userMapper.updateUserRole(id,userRole);
    }

    @Override
    public List<User> queryUsers() {
        return userMapper.selectList(null);
    }

}




