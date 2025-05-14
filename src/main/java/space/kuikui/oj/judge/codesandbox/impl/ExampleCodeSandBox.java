package space.kuikui.oj.judge.codesandbox.impl;

import space.kuikui.oj.judge.codesandbox.CodeSandBox;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeResponse;
import space.kuikui.oj.model.entity.JudgeInfo;

import java.util.List;

/**
 * 事例代码沙箱
 * @author kuikui
 * @date 2025/4/5 17:56
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> inputList = executeCodeRequest.getInputList();
        String code =  executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("通过");
        judgeInfo.setTime(1000l);
        judgeInfo.setMemory(100l);

        // 判题


        ExecuteCodeResponse.builder()
                .message("成功")
                .status(1)
                .judgeInfo(null)
                .outputList(null)
                .build();
        return executeCodeResponse;
    }
}
