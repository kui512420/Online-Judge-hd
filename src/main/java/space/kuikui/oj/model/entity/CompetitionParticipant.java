package space.kuikui.oj.model.entity;

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
    private Integer id;

    /**
     * 竞赛ID
     */
    private Integer competitionId;

    /**
     * 用户ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer userId;

    /**
     * 参与时间
     */
    private LocalDateTime joinTime;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 排名
     */
    private Integer rank;
}