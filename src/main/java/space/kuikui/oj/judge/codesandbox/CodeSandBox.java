package space.kuikui.oj.judge.codesandbox;

import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

/**
 * @author kuikui
 * @date 2025/4/5 17:46
 */
public interface CodeSandBox {

    /**
     * 执行代码
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException;

}
