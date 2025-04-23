package space.kuikui.oj.utils;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author kuikui
 * @date 2025/3/15 19:22
 * 发送邮箱和校验邮箱验证码
 */
@Component
public class CaptchaEmailUtils {
    @Autowired
    private RedisTemplate<String, String> redis;

    public static RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void getRedisTemplate() {
        redisTemplate = this.redis;
    }
    public String generateCaptcha(String email) {
        String code = RandomUtil.randomString(4);
        redisTemplate.opsForValue().set("email"+":"+email+":code", code,60000*3, TimeUnit.MILLISECONDS);
        return code;
    }
    public boolean check(String email, String cheakCode) {
        String key = "email"+":"+email+":code";
        String code = (String) redisTemplate.opsForValue().get(key);
        if(StringUtils.isAnyBlank(code)){
            return false;
        }
        if(code.equals(cheakCode)) {
            return true;
        }else{
            return false;
        }
    }

}
