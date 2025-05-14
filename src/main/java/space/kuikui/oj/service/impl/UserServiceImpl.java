package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.UserInfoRequest;
import space.kuikui.oj.model.dto.UserListRequest;
import space.kuikui.oj.model.entity.LoginLog;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.model.entity.UserRank;
import space.kuikui.oj.service.LoginLogService;
import space.kuikui.oj.service.RedisSetTokenExample;
import space.kuikui.oj.service.UserRankService;
import space.kuikui.oj.service.UserService;
import space.kuikui.oj.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.DigestUtils;
import space.kuikui.oj.utils.CaptchaEmailUtils;
import space.kuikui.oj.utils.CaptchaUtil;
import space.kuikui.oj.utils.IpUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static space.kuikui.oj.utils.IpUtil.getIpAddress;

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
    @Resource
    private LoginLogService loginLogService;
    @Resource
    private IpUtil ipUtil;
    @Resource
    private UserRankService userRankService;
    @Resource
    private RedisSetTokenExample redisSetTokenExample;
    @Value("${jwt.timeAccess}")
    private Long frtimeAccessom;
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
     * @todo 登录
     * @param user
     * @param userPassword
     * @param code
     * @param request
     * @return 双token
     */
    @Override
    public Map<String, String> userLogin(String user, String userPassword, String code, HttpServletRequest request) {
        // 获取设备信息
        String device = request.getHeader("User-Agent");
        String ip = getIpAddress(request);
        LoginLog loginLog = null;
        if(StringUtils.isAnyBlank(user,userPassword,code)){
            loginLog = LoginLog.builder().user(user).loginTime(new Date()).device(device).ip(ip).errorMsg("参数不能为空").status(0).build();
            loginLogService.addLoginLog(loginLog);
            throw new BusinessException(ErrorCode.PARMS_ERROR,"参数不能为空");
        }
        String encryptPassword =  DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user1 = userMapper.findUserByAccountAndPassowrd(user, encryptPassword);
        User user2 = userMapper.findUserByEmailAndPassowrd(user, encryptPassword);

        boolean cheakCode = captchaUtil.validateCode(request,code);
        if(!cheakCode){
            loginLog = LoginLog.builder().user(user).loginTime(new Date()).device(device).ip(ip).errorMsg("图片验证码错误").status(0).build();
            loginLogService.addLoginLog(loginLog);
            throw new BusinessException(ErrorCode.PARMS_ERROR,"图片验证码错误");
        }
        Map<String, String> map = new HashMap<>();
        if(user1==null && user2==null){
            loginLog = LoginLog.builder().user(user).loginTime(new Date()).device(device).ip(ip).errorMsg("账号或密码错误").status(0).build();
            loginLogService.addLoginLog(loginLog);
            throw new BusinessException(ErrorCode.PARMS_ERROR, "账号或密码错误");
        }else{
            // 检查用户是否被禁用
            User currentUser = user1 != null ? user1 : user2;
            if ("ban".equals(currentUser.getUserRole())) {
                loginLog = LoginLog.builder().user(user).loginTime(new Date()).device(device).ip(ip).errorMsg("账号已被禁用").status(0).build();
                loginLogService.addLoginLog(loginLog);
                throw new BusinessException(ErrorCode.FORBINDDEN_ERROR, "账号已被禁用，请联系管理员");
            }
            
            if(user1!=null){
                redisSetTokenExample.deleteAllTokensByUserId(user1.getId().toString());
                String accessToken = jwtLoginUtils.jwtBdAccess(user1);
                redisSetTokenExample.saveTokenToSet(String.valueOf(user1.getId()),accessToken,frtimeAccessom, TimeUnit.MILLISECONDS);
                map.put("AccessToken",accessToken);
            }else{
                redisSetTokenExample.deleteAllTokensByUserId(user2.getId().toString());
                String accessToken = jwtLoginUtils.jwtBdAccess(user2);
                redisSetTokenExample.saveTokenToSet(String.valueOf(user2.getId()),accessToken,frtimeAccessom, TimeUnit.MILLISECONDS);
                map.put("AccessToken",accessToken);
            }
            loginLog = LoginLog.builder().user(user).loginTime(new Date()).device(device).ip(ip).errorMsg("登录成功").status(1).build();
            loginLogService.addLoginLog(loginLog);
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
            int insertCount = userMapper.insert(user);
            if (insertCount!=1) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
            }else{
                // 把用户加入排行榜
                UserRank userRank = UserRank.builder()
                    .userId(user.getId())
                    .userName("用户" + user.getId()) // 设置默认用户名
                    .userAvatar("/default.png") // 设置默认头像
                    .acceptCount(0)
                    .submitCount(0)
                    .build();
                userRankService.addUserRank(userRank);
                
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userAccount", userAccount);
                User user1 = userMapper.selectOne(queryWrapper);
                String accessToken = jwtLoginUtils.jwtBdAccess(user1);
                Map<String,String> map = new HashMap<>();
                redisSetTokenExample.saveTokenToSet(String.valueOf(user.getId()),accessToken,frtimeAccessom, TimeUnit.MILLISECONDS);
                map.put("AccessToken",accessToken);
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
            // 同时更新排行榜中的用户名
            if (count > 0) {
                UserRank userRank = userRankService.getUserRankByUserId(id);
                if (userRank != null) {
                    userRank.setUserName(userName);
                    userRankService.updateUserRank(userRank);
                    // 更新Redis中的用户信息
                    redisSetTokenExample.deleteAllTokensByUserId(String.valueOf(id));
                }
            }
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
        int count = userMapper.updateUserAvatar(id, userAvatar);
        if (count > 0) {
            // 同时更新排行榜中的用户头像
            UserRank userRank = userRankService.getUserRankByUserId(id);
            if (userRank != null) {
                userRank.setUserAvatar(userAvatar);
                userRankService.updateUserRank(userRank);
            }
        }
        return count;
    }

    @Override
    public int logicalDelete(long id) {
        return userMapper.logicalDelete(id);
    }

    @Override
    public Integer putUserRole(Long id, String userRole) {
        Integer result = userMapper.updateUserRole(id,userRole);
        // 如果用户被禁用，清除其所有token
        if (result > 0 && "ban".equals(userRole)) {
            redisSetTokenExample.deleteAllTokensByUserId(String.valueOf(id));
        }
        return result;
    }

    @Override
    public List<User> queryUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public Integer updateInfo(UserInfoRequest userInfoRequest) {
        // 验证参数
        if (userInfoRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "用户ID不能为空");
        }
        
        // 检查用户是否存在
        User user = userMapper.findUserById(userInfoRequest.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户不存在");
        }
        
        // 如果请求中包含密码，则需要加密
        if (userInfoRequest.getPassword() != null && !userInfoRequest.getPassword().isEmpty()) {
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userInfoRequest.getPassword()).getBytes());
            userInfoRequest.setPassword(encryptPassword);
        }
        
        // 执行更新
        Integer result = userMapper.updateInfo(userInfoRequest);
        
        // 如果更新成功且包含用户名，同时更新排行榜中的用户名
        if (result > 0 && userInfoRequest.getUserName() != null && !userInfoRequest.getUserName().isEmpty()) {
            UserRank userRank = userRankService.getUserRankByUserId(userInfoRequest.getId());
            if (userRank != null) {
                userRank.setUserName(userInfoRequest.getUserName());
                userRankService.updateUserRank(userRank);
            }
        }
        
        // 如果用户被禁用，清除其所有token
        if (result > 0 && "ban".equals(userInfoRequest.getUserRole())) {
            redisSetTokenExample.deleteAllTokensByUserId(String.valueOf(userInfoRequest.getId()));
        }
        
        return result;
    }

}




