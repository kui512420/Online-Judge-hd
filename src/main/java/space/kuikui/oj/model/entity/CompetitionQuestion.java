package space.kuikui.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛题目关联
 * @author kuikui
 * @date 2025/4/28 15:30
 */
@Data
@TableName("competition_question")
public class CompetitionQuestion {
    /**
     * ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer id;

    /**
     * 竞赛ID
     */
    @TableField("competition_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long competitionId;

    /**
     * 题目ID
     */
    @TableField("question_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long questionId;

    /**
     * 题目分值
     */
    private Integer score;

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