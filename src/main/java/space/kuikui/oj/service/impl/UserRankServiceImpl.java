package space.kuikui.oj.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import space.kuikui.oj.mapper.UserRankMapper;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.model.entity.UserRank;
import space.kuikui.oj.service.UserRankService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserRankServiceImpl implements UserRankService {

    private static final String ACCEPT_COUNT_KEY = "ranking:accept_count";
    private static final String SUBMIT_COUNT_KEY = "ranking:submit_count";
    private static final String ACCEPT_RATE_KEY = "ranking:accept_rate";
    private static final String USER_INFO_KEY = "ranking:user_info";

    @Resource
    private UserRankMapper userRankMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        updateRankingData();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // 每30分钟执行一次
    @Override
    public void updateRankingData() {
        // 从数据库中获取排行榜数据
        List<UserRank> userRankList = userRankMapper.selectList(null);
        
        // 清空Redis中的排行榜数据
        redisTemplate.delete(ACCEPT_COUNT_KEY);
        redisTemplate.delete(SUBMIT_COUNT_KEY);
        redisTemplate.delete(ACCEPT_RATE_KEY);

        // 将排行榜数据存储到Redis中
        for (UserRank user : userRankList) {
            // 存储通过题目数量排行
            redisTemplate.opsForZSet().add(ACCEPT_COUNT_KEY, user.getUserId(), user.getAcceptCount());
            // 存储提交数量排行
            redisTemplate.opsForZSet().add(SUBMIT_COUNT_KEY, user.getUserId(), user.getSubmitCount());
            // 计算并存储通过率排行（只有提交数大于0的用户）
            if (user.getSubmitCount() > 0) {
                double acceptRate = (double) user.getAcceptCount() * 100 / user.getSubmitCount();
                redisTemplate.opsForZSet().add(ACCEPT_RATE_KEY, user.getUserId(), acceptRate);
            }
            // 存储用户信息
            redisTemplate.opsForHash().put(USER_INFO_KEY, user.getUserId().toString(), user);
        }
    }

    @Override
    public int addUserRank(UserRank userRank) {
        int result = userRankMapper.insert(userRank);
        if (result > 0) {
            updateUserRankInRedis(userRank);
        }
        return result;
    }

    @Override
    public int updateUserRank(UserRank userRank) {
        int result = userRankMapper.updateById(userRank);
        if (result > 0) {
            updateUserRankInRedis(userRank);
        }
        return result;
    }

    @Override
    public Page<UserRank> getTopUsersByAcceptCount(Integer current, Integer pageSize) {
        // 创建分页对象
        Page<UserRank> page = new Page<>(current, pageSize);
        
        // 获取总数
        Long total = redisTemplate.opsForZSet().size(ACCEPT_COUNT_KEY);
        if (total == null || total == 0) {
            page.setTotal(0);
            return page;
        }
        
        // 分页查询
        long start = (current - 1) * pageSize;
        long end = start + pageSize - 1;
        Set<Object> topUserIds = redisTemplate.opsForZSet().reverseRange(ACCEPT_COUNT_KEY, start, end);
        
        // 从Redis获取用户详细信息
        List<UserRank> userRankList = getUserRankListFromRedis(topUserIds);
        
        // 设置分页信息和结果
        page.setTotal(total);
        page.setRecords(userRankList);
        
        return page;
    }

    @Override
    public Page<UserRank> getTopUsersBySubmitCount(Integer current, Integer pageSize) {
        // 创建分页对象
        Page<UserRank> page = new Page<>(current, pageSize);
        
        // 获取总数
        Long total = redisTemplate.opsForZSet().size(SUBMIT_COUNT_KEY);
        if (total == null || total == 0) {
            page.setTotal(0);
            return page;
        }
        
        // 分页查询
        long start = (current - 1) * pageSize;
        long end = start + pageSize - 1;
        Set<Object> topUserIds = redisTemplate.opsForZSet().reverseRange(SUBMIT_COUNT_KEY, start, end);
        
        // 从Redis获取用户详细信息
        List<UserRank> userRankList = getUserRankListFromRedis(topUserIds);
        
        // 设置分页信息和结果
        page.setTotal(total);
        page.setRecords(userRankList);
        
        return page;
    }

    @Override
    public Page<UserRank> getTopUsersByAcceptRate(Integer current, Integer pageSize) {
        // 创建分页对象
        Page<UserRank> page = new Page<>(current, pageSize);
        
        // 获取总数
        Long total = redisTemplate.opsForZSet().size(ACCEPT_RATE_KEY);
        if (total == null || total == 0) {
            page.setTotal(0);
            return page;
        }
        
        // 分页查询
        long start = (current - 1) * pageSize;
        long end = start + pageSize - 1;
        Set<Object> topUserIds = redisTemplate.opsForZSet().reverseRange(ACCEPT_RATE_KEY, start, end);
        
        // 从Redis获取用户详细信息
        List<UserRank> userRankList = getUserRankListFromRedis(topUserIds);
        
        // 设置分页信息和结果
        page.setTotal(total);
        page.setRecords(userRankList);
        
        return page;
    }

    @Override
    public UserRank getUserRankByUserId(Long userId) {
        Object userObj = redisTemplate.opsForHash().get(USER_INFO_KEY, userId.toString());
        if (userObj != null) {
            return (UserRank) userObj;
        }
        return userRankMapper.selectUserRankByUserId(userId);
    }

    private List<UserRank> getUserRankListFromRedis(Set<Object> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserRank> result = new ArrayList<>();
        for (Object userId : userIds) {
            Object userObj = redisTemplate.opsForHash().get(USER_INFO_KEY, userId.toString());
            if (userObj != null) {
                result.add((UserRank) userObj);
            }
        }
        return result;
    }

    private void updateUserRankInRedis(UserRank userRank) {
        // 更新通过题目数量排行
        redisTemplate.opsForZSet().add(ACCEPT_COUNT_KEY, userRank.getUserId(), userRank.getAcceptCount());
        // 更新提交数量排行
        redisTemplate.opsForZSet().add(SUBMIT_COUNT_KEY, userRank.getUserId(), userRank.getSubmitCount());
        // 更新通过率排行
        if (userRank.getSubmitCount() > 0) {
            double acceptRate = (double) userRank.getAcceptCount() * 100 / userRank.getSubmitCount();
            redisTemplate.opsForZSet().add(ACCEPT_RATE_KEY, userRank.getUserId(), acceptRate);
        }
        // 更新用户信息
        redisTemplate.opsForHash().put(USER_INFO_KEY, userRank.getUserId().toString(), userRank);
    }
}
