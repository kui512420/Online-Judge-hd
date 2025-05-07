package space.kuikui.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import space.kuikui.oj.model.entity.Question;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kuikui
 * @date 2025/3/16 19:31
 */
@Data
public class QuestionListVo implements Serializable {

    public QuestionListVo(Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.createTime = question.getCreateTime();
        this.tags = question.getTags();
        this.submitNum = question.getSubmitNum();
        this.updateTime = question.getUpdateTime();
        this.acceptedNum = question.getAcceptedNum();
        this.userId = question.getUserId();
    }
    /**
     * id
     */

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签列表（json 数组）
     */
    private String tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 创建用户 id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
