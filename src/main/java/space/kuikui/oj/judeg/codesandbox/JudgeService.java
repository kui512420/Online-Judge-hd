package space.kuikui.oj.judeg.codesandbox;

import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeResponse;

/**
 * @author kuikui
 * @date 2025/4/5 18:53
 */
public interface JudgeService {

    ExecuteCodeResponse executeCode(Long questionId);
}
