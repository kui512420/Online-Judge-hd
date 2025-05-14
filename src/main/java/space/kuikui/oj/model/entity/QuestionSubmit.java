package space.kuikui.oj.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author kuikui
 * @date 2025/4/7 16:43
 */
@Data
public class QuestionSubmit {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String language;
    private String code;
    private String judgeInfo;
    private Integer status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long questionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long competitionId;
    private Date createTime;
    private Date updateTime;
    private Integer isDelete;
}
