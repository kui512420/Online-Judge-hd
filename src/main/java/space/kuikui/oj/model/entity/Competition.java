package space.kuikui.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuikui
 * @date 2025/4/28 15:06
 */
@Data
public class Competition {
    /**
     * 竞赛ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 竞赛名称
     */
    private String name;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long creatorId;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 状态:0未开始,1进行中,2已结束
     */
    private Integer status;

    /**
     * 竞赛描述
     */
    private String description;

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

    /**
     * 是否删除:0否,1是
     */
    @TableField("is_deleted")
    private Integer isDeleted;
}