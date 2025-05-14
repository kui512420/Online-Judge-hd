package space.kuikui.oj.judge.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import space.kuikui.oj.judge.codesandbox.CodeSandBox;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeResponse;
import space.kuikui.oj.model.entity.JudgeInfo;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kuikui
 * @date 2025/4/5 18:21
 */
public class RemoteCodeSandBox implements CodeSandBox {

    private static final String TEMP_PATH = System.getProperty("user.dir") + File.separator + "temp";
    private static final String TEMP_FILE_NAME = "Main.java";

    // 定义危险代码的正则表达式模式列表
    private static final List<Pattern> DANGEROUS_CODE_PATTERNS = Arrays.asList(
            // 禁止执行系统命令
            Pattern.compile("Runtime\\.getRuntime\\(\\)\\.exec\\("),
            // 禁止使用ProcessBuilder
            Pattern.compile("ProcessBuilder"),
            // 禁止通过反射访问系统类
            Pattern.compile("System\\.getSecurityManager\\(\\)"),
            // 禁止使用SecurityManager
            Pattern.compile("java\\.lang\\.SecurityManager"),
            // 禁止反射
            Pattern.compile("java\\.lang\\.reflect"),
            // 禁止文件操作
            Pattern.compile("java\\.io\\.File(?!Writer)"),
            Pattern.compile("java\\.nio\\.file"),
            // 禁止网络操作
            Pattern.compile("java\\.net\\.Socket"),
            Pattern.compile("java\\.net\\.ServerSocket"),
            Pattern.compile("java\\.net\\.URL"),
            // 禁止线程操作
            Pattern.compile("java\\.lang\\.Thread\\.sleep"),
            // 禁止类加载器
            Pattern.compile("ClassLoader"),
            // 禁止第三方unsafe包
            Pattern.compile("sun\\.misc\\.Unsafe")
    );
    /**
     * 检查代码是否包含危险操作
     * @param code 要检查的代码
     * @return 如果包含危险代码，返回错误消息；否则返回null
     */
    private static String checkDangerousCode(String code) {
        for (Pattern pattern : DANGEROUS_CODE_PATTERNS) {
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                return "代码中包含不允许的操作: " + matcher.group();
            }
        }
        return null;
    }


    /**
     * 执行单个测试用例
     *
     * @param code      提交的代码
     * @param inputData 输入数据
     * @throws InterruptedException
     * @throws IOException
     */
    public void judeg(String code, String inputData) throws InterruptedException, IOException {
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
        // 添加安全管理器参数限制代码执行权限
        Process run = Runtime.getRuntime().exec(
                new String[]{"java", "-Dfile.encoding=UTF-8",
                        // 限制执行时间（5秒）
                        "-Xmx256m", "-Xss256k",
                        // 禁止系统调用和文件访问
                        "-Djava.security.manager",
                        "-Djava.security.policy==" + System.getProperty("user.dir") + File.separator + "security.policy",
                        "Main"},
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
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException {

        ExecuteCodeResponse response = new ExecuteCodeResponse();

        // 首先检查代码安全性
        String dangerousCodeMessage = checkDangerousCode(executeCodeRequest.getCode());
        if (dangerousCodeMessage != null) {
            // 存在安全问题 处理
            response.setMessage(dangerousCodeMessage);
            response.setStatus(3);
            return response;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> outputList = new ArrayList<>();
        Question question = executeCodeRequest.getQuestion();
        String code = executeCodeRequest.getCode();
        boolean allPassed = true;
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
                judeg(code, inputData);
            } finally {
                System.setOut(originalOut);
            }
            Long endTime = System.currentTimeMillis();
            String actualOutput = outputStream.toString().trim();
            outputList.add(actualOutput);
            judgeInfo.setUserOutput(actualOutput);
            // 验证输出结果
            if(actualOutput.equals(expectedOutput)){
                judgeInfo.setPassed(2);
                judgeInfo.setMessage("通过");
            }else{
                judgeInfo.setPassed(3);
                judgeInfo.setMessage("未通过");
                allPassed = false;
            }
            judgeInfo.setTime(endTime-startTime);
            judgeInfoList.add(judgeInfo);
        }
        if (allPassed) {
            response.setMessage("通过");
            response.setStatus(2);
            //response.setJudgeInfo();
        } else {
            response.setMessage("未通过");
            response.setStatus(3);
        }
        response.setJudgeInfo(judgeInfoList);
        response.setOutputList(outputList);
        return response;


    }
}
