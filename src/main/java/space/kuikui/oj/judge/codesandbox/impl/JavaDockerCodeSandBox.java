package space.kuikui.oj.judge.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import space.kuikui.oj.judge.codesandbox.CodeSandBox;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeResponse;
import space.kuikui.oj.model.entity.JudgeInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于Docker的代码沙箱实现
 * @author kuikui
 * @date 2025/5/1 9:44
 */
@Component
@Slf4j
public class JavaDockerCodeSandBox implements CodeSandBox {
    
    private static final long TIMEOUT = 10000L; // 10秒超时
    private static final String JAVA_IMAGE = "openjdk:17-slim"; // 使用精简版Java镜像
    private static final String TEMP_PATH = System.getProperty("user.dir") + File.separator + "docker_temp";
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
    private String checkDangerousCode(String code) {
        for (Pattern pattern : DANGEROUS_CODE_PATTERNS) {
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                return "代码中包含不允许的操作: " + matcher.group();
            }
        }
        return null;
    }
    
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();
        
        // 1. 检查代码安全性
        String dangerousCodeMessage = checkDangerousCode(code);
        if (dangerousCodeMessage != null) {
            ExecuteCodeResponse response = new ExecuteCodeResponse();
            response.setMessage(dangerousCodeMessage);
            response.setStatus(0);
            return response;
        }
        
        // 2. 保存代码到临时文件
        String workDir = createWorkDir();
        File codeFile = new File(workDir, TEMP_FILE_NAME);
        FileWriter fileWriter = new FileWriter(codeFile);
        fileWriter.write(code);
        
        DockerClient dockerClient = null;
        String containerId = null;
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        List<JudgeInfo> judgeInfoList = new ArrayList<>();
        
        try {
            // 3. 初始化Docker客户端
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
            
            // 4. 拉取Java镜像（如果本地没有）
            try {
                dockerClient.pullImageCmd(JAVA_IMAGE)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("拉取镜像失败", e);
                response.setMessage("系统错误：拉取Docker镜像失败");
                response.setStatus(0);
                return response;
            }
            
            // 5. 创建并启动容器
            HostConfig hostConfig = new HostConfig()
                    .withBinds(new Bind(workDir, new Volume("/app")))
                    .withMemory(256 * 1024 * 1024L) // 内存限制：256MB
                    .withMemorySwap(0L) // 禁用交换内存
                    .withCpuCount(1L) // CPU限制
                    .withNetworkMode("none") // 禁用网络
                    .withReadonlyRootfs(true); // 根文件系统只读
            
            CreateContainerResponse containerResponse = dockerClient.createContainerCmd(JAVA_IMAGE)
                    .withHostConfig(hostConfig)
                    .withWorkingDir("/app")
                    .withTty(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            
            containerId = containerResponse.getId();
            dockerClient.startContainerCmd(containerId).exec();
            
            // 6. 在容器中编译Java代码
            ExecCreateCmdResponse compileExecResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("javac", "-encoding", "utf-8", TEMP_FILE_NAME)
                    .exec();
            
            ByteArrayOutputStream compileStdout = new ByteArrayOutputStream();
            ByteArrayOutputStream compileStderr = new ByteArrayOutputStream();
            dockerClient.execStartCmd(compileExecResponse.getId())
                    .exec(new ExecStartResultCallback(compileStdout, compileStderr))
                    .awaitCompletion(TIMEOUT, TimeUnit.MILLISECONDS);
            
            // 检查编译错误
            String compileError = compileStderr.toString(StandardCharsets.UTF_8);
            if (StrUtil.isNotBlank(compileError)) {
                response.setMessage("编译错误: " + compileError);
                response.setStatus(0);
                return response;
            }
            
            // 7. 执行每个测试用例
            for (int i = 0; i < inputList.size(); i++) {
                String input = inputList.get(i);
                
                // 创建输入文件
                FileWriter inputFileWriter = new FileWriter(new File(workDir, "input.txt"));
                inputFileWriter.write(input);
                
                // 运行Java程序（使用安全管理器）
                ExecCreateCmdResponse runExecResponse = dockerClient.execCreateCmd(containerId)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .withCmd("java", "-Dfile.encoding=UTF-8", 
                                "-Xmx128m", "-Xss128k", // 内存和栈限制
                                "-Djava.security.manager", // 启用安全管理器
                                "Main", "<", "input.txt")
                        .exec();
                
                // 设置超时机制捕获输出
                ByteArrayOutputStream runStdout = new ByteArrayOutputStream();
                ByteArrayOutputStream runStderr = new ByteArrayOutputStream();
                
                long startTime = System.currentTimeMillis();
                dockerClient.execStartCmd(runExecResponse.getId())
                        .exec(new ExecStartResultCallback(runStdout, runStderr))
                        .awaitCompletion(TIMEOUT, TimeUnit.MILLISECONDS);
                long endTime = System.currentTimeMillis();
                
                // 获取程序输出
                String runOutput = runStdout.toString(StandardCharsets.UTF_8).trim();
                String runError = runStderr.toString(StandardCharsets.UTF_8);
                
                // 记录执行结果
                outputList.add(runOutput);
                
                // 创建判题信息
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setTime(endTime - startTime);
                judgeInfo.setMemory(256L); // 最大内存使用（MB）
                
                if (StrUtil.isNotBlank(runError)) {
                    judgeInfo.setMessage("运行错误: " + runError);
                    judgeInfo.setPassed(0);
                } else {
                    judgeInfo.setMessage("执行成功");
                    judgeInfo.setPassed(1);
                    judgeInfo.setUserOutput(runOutput);
                }
                
                judgeInfoList.add(judgeInfo);
            }
            
            // 8. 设置返回结果
            response.setMessage("执行成功");
            response.setStatus(1);
            response.setOutputList(outputList);
            response.setJudgeInfo(judgeInfoList);
            
        } catch (Exception e) {
            log.error("Docker沙箱执行异常", e);
            response.setMessage("系统错误: " + e.getMessage());
            response.setStatus(0);
        } finally {
            // 9. 清理资源
            try {
                if (dockerClient != null && containerId != null) {
                    // 停止并移除容器
                    dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
                    dockerClient.removeContainerCmd(containerId).exec();
                }
            } catch (Exception e) {
                log.error("清理Docker容器失败", e);
            }
            
            // 清理临时文件
            try {
                FileUtil.del(workDir);
            } catch (Exception e) {
                log.error("清理临时文件失败", e);
            }
        }
        
        return response;
    }
    
    /**
     * 创建临时工作目录
     */
    private String createWorkDir() {
        String dirName = UUID.randomUUID().toString();
        String workDir = TEMP_PATH + File.separator + dirName;
        FileUtil.mkdir(workDir);
        return workDir;
    }
    
    /**
     * Docker镜像拉取回调类
     */
    private static class PullImageResultCallback extends com.github.dockerjava.core.command.PullImageResultCallback {
        @Override
        public void onNext(PullResponseItem item) {
            super.onNext(item);
        }
    }
}
