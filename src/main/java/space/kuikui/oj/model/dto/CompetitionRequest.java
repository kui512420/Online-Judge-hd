package space.kuikui.oj.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛查询请求
 * @author kuikui
 * @date 2025/4/28 15:35
 */
@Data
public class CompetitionRequest {
    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 竞赛名称（模糊查询）
     */
    private String name;

    /**
     * 竞赛状态：0-未开始，1-进行中，2-已结束
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 开始时间范围-起始
     */
    private LocalDateTime startTimeBegin;

    /**
     * 开始时间范围-结束
     */
    private LocalDateTime startTimeEnd;
} 