package space.kuikui.oj.judeg.codesandbox.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import space.kuikui.oj.judeg.codesandbox.CodeSandBox;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeResponse;

import java.util.List;

/**
 * @author kuikui
 * @date 2025/5/1 9:44
 */
public class JavaDockerCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container : containers) {
            System.out.println(container.toString());
        }


        return null;
    }

    public static void main(String[] args) {
        new JavaDockerCodeSandBox().executeCode(new ExecuteCodeRequest());
    }
}
