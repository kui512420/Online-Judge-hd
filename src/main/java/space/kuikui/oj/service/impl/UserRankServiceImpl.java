package space.kuikui.oj.service.impl;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import space.kuikui.oj.mapper.UserRankMapper;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.model.entity.UserRank;
import space.kuikui.oj.service.UserRankService;

import java.util.List;

@Service
public class UserRankServiceImpl implements UserRankService {

    @Resource
    private UserRankMapper userRankMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void updateRankingData() {
        // 从数据库中获取排行榜数据
        List<UserRank> userRankList = userRankMapper.selectList(null);
        // 清空Redis中的排行榜数据
        redisTemplate.delete("ranking");

        // 将排行榜数据存储到Redis中
        for (UserRank user : userRankList) {
            redisTemplate.opsForZSet().add("ranking", user.getUserId(), user.getSubmitCount());
            // 存储用户的名称和头像到哈希
            String userHashKey = "user:" + user.getUserId();
            redisTemplate.opsForHash().put(userHashKey, "name", user.getUserName());
            redisTemplate.opsForHash().put(userHashKey, "avatar", user.getUserAvatar());
        }
    }
    /**
     * 把用户加入排行榜
     * @param userRank
     * @return
     */
    @Override
    public int addUserRank(UserRank userRank) {
        return userRankMapper.insert(userRank);
    }
}
