package space.kuikui.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import space.kuikui.oj.model.entity.Question;

import java.io.Serializable;

/**
 * @author kuikui
 * @date 2025/3/31 21:16
 */
@Data
public class QuestionViewVo implements Serializable {

    public QuestionViewVo(Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.content = question.getContent();
        this.judgeConfig = question.getJudgeConfig();
    }
    private Long id;
    private String title;
    private String content;
    private String judgeConfig;
}
