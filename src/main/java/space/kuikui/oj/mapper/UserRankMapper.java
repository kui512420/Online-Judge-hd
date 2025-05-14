package space.kuikui.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import space.kuikui.oj.model.entity.UserRank;

import java.util.List;

@Mapper
public interface UserRankMapper extends BaseMapper<UserRank> {
    /**
     * 查询用户排行榜列表（按通过题目数量降序排序）
     * @param limit 返回记录数量
     * @return 用户排行榜列表
     */
    @Select("SELECT * FROM user_rank ORDER BY accept_count DESC LIMIT #{limit}")
    List<UserRank> selectTopUsersByAcceptCount(@Param("limit") Integer limit);

    /**
     * 查询用户排行榜列表（按提交数量降序排序）
     * @param limit 返回记录数量
     * @return 用户排行榜列表
     */
    @Select("SELECT * FROM user_rank ORDER BY submit_count DESC LIMIT #{limit}")
    List<UserRank> selectTopUsersBySubmitCount(@Param("limit") Integer limit);

    /**
     * 查询用户排行榜列表（按通过率降序排序，要求至少提交过一次）
     * @param limit 返回记录数量
     * @return 用户排行榜列表
     */
    @Select("SELECT *, (accept_count * 100.0 / submit_count) as accept_rate " +
            "FROM user_rank " +
            "WHERE submit_count > 0 " +
            "ORDER BY accept_rate DESC LIMIT #{limit}")
    List<UserRank> selectTopUsersByAcceptRate(@Param("limit") Integer limit);

    /**
     * 获取用户的排名信息
     * @param userId 用户ID
     * @return 用户排名信息
     */
    @Select("SELECT * FROM user_rank WHERE user_id = #{userId}")
    UserRank selectUserRankByUserId(@Param("userId") Long userId);
}
