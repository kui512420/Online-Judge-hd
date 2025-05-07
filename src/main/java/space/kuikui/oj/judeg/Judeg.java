package space.kuikui.oj.judeg;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeResponse;
import space.kuikui.oj.model.entity.JudgeInfo;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author kuikui
 * @date 2025/4/7 15:27
 */
@Component
public class Judeg {
    private static final String TEMP_PATH = System.getProperty("user.dir") + File.separator + "temp";
    private static final String TEMP_FILE_NAME = "Main.java";

    /**
     * 运行测试用例
     *
     * @param code     提交的代码
     * @param question 问题对象，包含测试用例
     * @throws InterruptedException
     * @throws IOException
     */
    public ExecuteCodeResponse judgeAllTestCases(String code, Question question) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        boolean allPassed = true;
        if (question.getJudgeCase() == null || question.getJudgeCase().isEmpty()) {
            response.setMessage("没有可用的测试用例");
            response.setStatus(0);
            response.setOutputList(outputList);
            return response;
        }
        List<JudgeInfo> judgeInfoList = new ArrayList<>();
        for (TestCase oj : objectMapper.readValue(question.getJudgeCase(), TestCase[].class)) {

            JudgeInfo judgeInfo= new JudgeInfo();
            String inputData = oj.getInput();
            String expectedOutput = oj.getOutput();

            // 捕获程序的输出
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));
            Long startTime = System.currentTimeMillis();

            try {
                judeg(code, inputData, question);
            } finally {
                System.setOut(originalOut);
            }
            Long endTime = System.currentTimeMillis();
            String actualOutput = outputStream.toString().trim();
            outputList.add(actualOutput);
            judgeInfo.setUserOutput(actualOutput);
            // 验证输出结果
            if(actualOutput.equals(expectedOutput)){
                response.setMessage("通过");
                judgeInfo.setPassed(1);
            }else{
                judgeInfo.setPassed(0);
                response.setMessage("未通过");
                allPassed = false;
            }
            judgeInfo.setTime(endTime-startTime);
            judgeInfoList.add(judgeInfo);
        }
        if (allPassed) {
            response.setMessage("通过");
            response.setStatus(1);
            //response.setJudgeInfo();
        } else {
            response.setMessage("未通过");
            response.setStatus(0);
        }
        response.setJudgeInfo(judgeInfoList);
        response.setOutputList(outputList);
        return response;
    }

    /**
     * 执行单个测试用例
     *
     * @param code      提交的代码
     * @param inputData 输入数据
     * @param question  问题对象
     * @throws InterruptedException
     * @throws IOException
     */
    public void judeg(String code, String inputData, Question question) throws InterruptedException, IOException {
        // 读取源文件
        String result = code;
        // 创建临时目录
        long dirName = new Date().getTime();
        String workDir = TEMP_PATH + File.separator + dirName;
        FileUtil.mkdir(workDir);

        // 写入Java文件
        String javaFile = workDir + File.separator + TEMP_FILE_NAME;
        new FileWriter(javaFile).write(result);

        // 编译（指定工作目录）
        Process compile = Runtime.getRuntime().exec(
                new String[]{"javac", "-encoding", "utf-8", TEMP_FILE_NAME},
                null,  // 环境变量（null表示继承当前环境）
                new File(workDir)  // 工作目录
        );
        if (compile.waitFor() != 0) {
            System.err.println("编译失败");
            printStream(compile.getErrorStream());
            return;
        }

        // 运行（指定工作目录和类路径）
        Process run = Runtime.getRuntime().exec(
                new String[]{"java", "-Dfile.encoding=UTF-8", "Main"},
                null,
                new File(workDir)
        );

        // 模拟输入，将输入数据写入到运行程序的标准输入流中
        try (PrintWriter writer = new PrintWriter(run.getOutputStream())) {
            writer.println(inputData);
            writer.flush();
        }

        // 处理输出
        if (run.waitFor() == 0) {
            printStream(run.getInputStream());
        } else {
            System.err.println("运行失败");
            printStream(run.getErrorStream());
        }
    }

    private static void printStream(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}