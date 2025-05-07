package space.kuikui.oj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisSetTokenExample {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveTokenToSet(String userId, String token, long expiration, TimeUnit timeUnit) {
        // 使用用户 ID 作为集合的键
        String key = "user_tokens:" + userId;
        // 将 token 添加到集合中
        redisTemplate.opsForSet().add(key, token);
        // 设置集合的过期时间
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public boolean isTokenExistsInSet(String userId, String token) {
        String key = "user_tokens:" + userId;
        // 检查 token 是否存在于集合中
        return redisTemplate.opsForSet().isMember(key, token);
    }
    public boolean deleteTokenFromSet(String userId, String token) {
        String key = "user_tokens:" + userId;
        // 从集合中删除指定的 token
        Long result = redisTemplate.opsForSet().remove(key, token);
        return result != null && result > 0;
    }
    public long getTokenRemainingTime(String userId, TimeUnit timeUnit) {
        String key = "user_tokens:" + userId;
        // 获取集合的剩余过期时间
        return redisTemplate.getExpire(key, timeUnit);
    }
    public boolean deleteAllTokensByUserId(String userId) {
        String key = "user_tokens:" + userId;
        // 删除指定用户 ID 对应的集合
        return redisTemplate.delete(key);
    }
}