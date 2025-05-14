package space.kuikui.oj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * token 在redis中的处理
 */
@Service
public class RedisSetTokenExample {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 保存token
     * @param userId
     * @param token
     * @param expiration
     * @param timeUnit
     */
    public void saveTokenToSet(String userId, String token, long expiration, TimeUnit timeUnit) {
        // 使用用户 ID 作为集合的键
        String key = "user_tokens:" + userId;
        // 将 token 添加到集合中
        redisTemplate.opsForSet().add(key, token);
        // 设置集合的过期时间
        redisTemplate.expire(key, expiration, timeUnit);
    }

    /**
     * 判断token是否存在
     * @param userId
     * @param token
     * @return
     */
    public boolean isTokenExistsInSet(String userId, String token) {
        String key = "user_tokens:" + userId;
        // 检查 token 是否存在于集合中
        return redisTemplate.opsForSet().isMember(key, token);
    }

    /**
     * 删除用户的token
     * @param userId
     * @param token
     * @return
     */
    public boolean deleteTokenFromSet(String userId, String token) {
        String key = "user_tokens:" + userId;
        // 从集合中删除指定的 token
        Long result = redisTemplate.opsForSet().remove(key, token);
        return result != null && result > 0;
    }

    /**
     * 获取用户token的剩余有效时长
     * @param userId
     * @param timeUnit
     * @return
     */
    public long getTokenRemainingTime(String userId, TimeUnit timeUnit) {
        String key = "user_tokens:" + userId;
        // 获取集合的剩余过期时间
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 删除用户下的所以token
     * @param userId
     * @return
     */
    public boolean deleteAllTokensByUserId(String userId) {
        String key = "user_tokens:" + userId;
        // 删除指定用户 ID 对应的集合
        return redisTemplate.delete(key);
    }

}