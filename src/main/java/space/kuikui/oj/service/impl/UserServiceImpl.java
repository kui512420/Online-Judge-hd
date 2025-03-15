package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.service.UserService;
import space.kuikui.oj.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.Resource;
import org.springframework.util.DigestUtils;
import space.kuikui.oj.utils.CaptchaEmailUtils;

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
    @Resource
    private CaptchaEmailUtils captchaEmailUtils;

    @Value("${spring.mail.username}")
    private String from;
    private static String SALT = "KUIKUI";

    @Override
    public String userRegister(String userAccount, String userPassword, String userCheakPassword, String email, String emailCode){
        if(StringUtils.isAnyBlank(userAccount,userPassword,userCheakPassword,email,emailCode)){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"参数不能为空");
        }else if(!(userAccount.length()>=6 && userAccount.length()<=15)){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"用户名限制：6~15位");
        }else if(!(userPassword.length()>=6 && userPassword.length()<=15)){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"密码限制：6~15位");
        }else if(!userPassword.equals(userCheakPassword)){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"两次密码输入不同");
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
            queryWrapperEmail.eq("userAccount", email);
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
                return user.getId()+"";
            }
        }
    }

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
        return txt;
    }
}




