package space.kuikui.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuikui
 * @date 2025/4/28 15:06
 */
@Data
public class CompetitionParticipant {
    /**
     * ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 竞赛ID
     */
    @TableField("competition_id")
    private Long competitionId;

    /**
     * 用户ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField("user_id")
    private Long userId;

    /**
     * 参与时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 排名
     */
    @TableField("`rank`")
    private Integer rank;
    
    /**
     * 是否已提交
     */
    @TableField("is_submitted")
    private Integer isSubmitted;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}