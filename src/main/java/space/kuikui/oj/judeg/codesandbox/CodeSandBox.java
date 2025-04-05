package space.kuikui.oj.judeg.codesandbox;

import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeResponse;

/**
 * @author kuikui
 * @date 2025/4/5 17:46
 */
public interface CodeSandBox {

    /**
     * 执行代码
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
