package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import space.kuikui.oj.model.entity.UserRank;

public interface UserRankService {
    /**
     * 更新排行榜数据
     */
    void updateRankingData();

    /**
     * 添加用户排名记录
     * @param userRank 用户排名信息
     * @return 影响行数
     */
    int addUserRank(UserRank userRank);

    /**
     * 更新用户排名记录
     * @param userRank 用户排名信息
     * @return 影响行数
     */
    int updateUserRank(UserRank userRank);

    /**
     * 获取通过题目数量排行榜
     * @param current 当前页码
     * @param pageSize 每页记录数
     * @return 分页排行榜列表
     */
    Page<UserRank> getTopUsersByAcceptCount(Integer current, Integer pageSize);

    /**
     * 获取提交数量排行榜
     * @param current 当前页码
     * @param pageSize 每页记录数
     * @return 分页排行榜列表
     */
    Page<UserRank> getTopUsersBySubmitCount(Integer current, Integer pageSize);

    /**
     * 获取通过率排行榜
     * @param current 当前页码
     * @param pageSize 每页记录数
     * @return 分页排行榜列表
     */
    Page<UserRank> getTopUsersByAcceptRate(Integer current, Integer pageSize);

    /**
     * 获取用户的排名信息
     * @param userId 用户ID
     * @return 用户排名信息
     */
    UserRank getUserRankByUserId(Long userId);
}
