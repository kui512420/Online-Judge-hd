package space.kuikui.oj.judeg.codesandbox.impl;

import space.kuikui.oj.judeg.codesandbox.CodeSandBox;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeResponse;
import space.kuikui.oj.judeg.codesandbox.model.enums.JudgeInfoMessageEnum;
import space.kuikui.oj.model.entity.JudgeInfo;

/**
 * 事例代码沙箱
 * @author kuikui
 * @date 2025/4/5 17:56
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        System.out.println("实例代码沙箱");
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setStatus(1000);
        executeCodeResponse.setMessage(String.valueOf(JudgeInfoMessageEnum.SUCCESS));
        executeCodeResponse.setOutputList(executeCodeRequest.getInputList());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("通过");
        judgeInfo.setTime(1000l);
        judgeInfo.setMemory(100l);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
