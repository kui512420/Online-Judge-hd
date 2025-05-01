package space.kuikui.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import space.kuikui.oj.model.entity.Competition;

import java.time.LocalDateTime;

/**
 * 竞赛信息VO
 * @author kuikui
 * @date 2025/4/28 15:45
 */
@Data
public class CompetitionVO {
    /**
     * 竞赛ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 竞赛名称
     */
    private String name;

    /**
     * 创建人ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long creatorId;
    
    /**
     * 创建人账号
     */
    private String creatorAccount;
    
    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态:0未开始,1进行中,2已结束
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 竞赛描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 参与人数
     */
    private Integer participantCount;
    
    /**
     * 题目数量
     */
    private Integer questionCount;
    
    /**
     * 从Competition转换为VO
     */
    public static CompetitionVO fromCompetition(Competition competition) {
        if (competition == null) {
            return null;
        }
        
        CompetitionVO vo = new CompetitionVO();
        vo.setId(competition.getId());
        vo.setName(competition.getName());
        vo.setCreatorId(competition.getCreatorId());
        vo.setStartTime(competition.getStartTime());
        vo.setEndTime(competition.getEndTime());
        vo.setStatus(competition.getStatus());
        vo.setDescription(competition.getDescription());
        vo.setCreateTime(competition.getCreateTime());
        
        // 设置状态描述
        if (competition.getStatus() != null) {
            switch (competition.getStatus()) {
                case 0:
                    vo.setStatusDesc("未开始");
                    break;
                case 1:
                    vo.setStatusDesc("进行中");
                    break;
                case 2:
                    vo.setStatusDesc("已结束");
                    break;
                default:
                    vo.setStatusDesc("未知");
            }
        }
        
        return vo;
    }
} 