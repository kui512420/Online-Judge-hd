package space.kuikui.oj.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 添加竞赛请求
 * @author kuikui
 * @date 2025/4/28 15:40
 */
@Data
public class CompetitionAddRequest {
    /**
     * 竞赛ID（编辑时使用）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    
    /**
     * 竞赛名称
     */
    private String name;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 竞赛描述
     */
    private String description;

    /**
     * 关联的题目ID列表
     */
    private List<Long> questionIds;

    /**
     * 题目分值列表（与题目ID对应）
     */
    private List<Integer> scores;
} 