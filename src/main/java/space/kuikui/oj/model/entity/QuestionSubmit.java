package space.kuikui.oj.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author kuikui
 * @date 2025/4/7 16:43
 */
@Data
public class QuestionSubmit {
    private Long id;
    private String language;
    private String code;
    private String judgeInfo;
    private Integer status;
    private Long questionId;
    private Long userId;
    private Date createTime;
    private Date updateTime;
    private Integer isDelete;
}
