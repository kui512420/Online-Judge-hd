package space.kuikui.oj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author kuikui
 * @date 2025/4/5 20:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPostRequest implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String tags;
    private Long userId;
    /**
     * 判题配置（json 对象）
     */
    private String judgeConfig;
    /**
     * 测试用例（json 对象）
     */
    private String judgeCase;
}
