package space.kuikui.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import space.kuikui.oj.model.entity.Competition;

/**
 * 竞赛Mapper
 * @author kuikui
 * @date 2025/4/28 15:50
 */
@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
    
    /**
     * 查询竞赛参与人数
     * @param competitionId 竞赛ID
     * @return 参与人数
     */
    @Select("SELECT COUNT(*) FROM competition_participant WHERE competition_id = #{competitionId}")
    Integer getParticipantCount(Long competitionId);

    /**
     * 查询竞赛题目数量
     * @param competitionId 竞赛ID
     * @return 题目数量
     */
    @Select("SELECT COUNT(*) FROM competition_question WHERE competition_id = #{competitionId}")
    Integer getQuestionCount(Long competitionId);
} 