package space.kuikui.oj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import space.kuikui.oj.model.entity.Question;

import java.util.List;

/**
 * @author kuikui
 * @date 2025/4/5 17:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 用户提供的测试数据
     */
    private List<String> inputList;
    /**
     * 用户编写的代码
     */
    private String code;
    /**
     * 题目的信息
     */
    private Question question;
    /**
     * 用户编写代码的编程语言
     */
    private String language;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 提交记录ID
     */
    private Long questionSubmitId;
}
