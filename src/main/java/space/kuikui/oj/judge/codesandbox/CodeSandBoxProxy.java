package space.kuikui.oj.judge.codesandbox;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

/**
 * @author kuikui
 * @date 2025/4/5 18:27
 */
@Slf4j
@AllArgsConstructor
public class CodeSandBoxProxy implements CodeSandBox{

    private  CodeSandBox codeSandBox;
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException {
        log.info("代码沙箱请求信息："+String.valueOf(executeCodeRequest));
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        log.info("代码沙箱返回信息："+String.valueOf(executeCodeResponse));
        return executeCodeResponse;
    }
}
