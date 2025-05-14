package space.kuikui.oj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import space.kuikui.oj.model.entity.JudgeInfo;

import java.util.List;

/**
 * @author kuikui
 * @date 2025/4/5 17:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {
    private List<String> outputList;
    /**
     * 执行信息
     */
    private String message;
    /**
     * 执行状态
     */
    private Integer status;
    /**
     *判题信息
     */
    private List<JudgeInfo> judgeInfo;

}
