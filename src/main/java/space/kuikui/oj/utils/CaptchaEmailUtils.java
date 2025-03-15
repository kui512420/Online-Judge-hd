package space.kuikui.oj.utils;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author kuikui
 * @date 2025/3/15 19:22
 * 发送邮箱和校验邮箱验证码
 */
@Component
public class CaptchaEmailUtils {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    public String generateCaptcha(String email) {
        String code = RandomUtil.randomString(4);
        redisTemplate.opsForValue().set("email"+":"+email+":code", code,60000*3, TimeUnit.MILLISECONDS);
        return code;
    }
    public boolean cheak(String email, String cheakCode) {
        String code = (String) redisTemplate.opsForValue().get("email"+":"+email+":code");
        return cheakCode==null?false:code.equals(cheakCode);
    }

}
