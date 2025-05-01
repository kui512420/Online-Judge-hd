package space.kuikui.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author kuikui
 * @date 2025/4/25 16:46
 */
@Data
public class Tag {
    /**
     * 标签的唯一标识，主键
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 创建时间，自动设置为当前时间
     */
    private Date createTime;

    /**
     * 更新时间，自动设置为当前时间，更新时自动更新
     */
    private Date updateTime;

    /**
     * 是否删除，0 表示未删除，1 表示已删除
     */
    private Integer isDelete;
}