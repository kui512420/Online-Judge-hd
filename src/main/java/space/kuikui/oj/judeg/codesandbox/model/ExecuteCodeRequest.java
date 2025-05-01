package space.kuikui.oj.judeg.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 用户编写代码的编程语言
     */
    private String language;
}
