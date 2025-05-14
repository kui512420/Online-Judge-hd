package space.kuikui.oj.judge.codesandbox;

import space.kuikui.oj.judge.codesandbox.impl.ExampleCodeSandBox;
import space.kuikui.oj.judge.codesandbox.impl.JavaDockerCodeSandBox;
import space.kuikui.oj.judge.codesandbox.impl.RemoteCodeSandBox;

/**
 * 代码沙箱工厂 （根据传入的字符串，创建不同的沙箱实例）
 * @author kuikui
 * @date 2025/4/5 18:08
 */
public class CodeSandBoxFactory {

    public static CodeSandBox newInstance(String type){
        switch (type) {
            case "example":
                return new ExampleCodeSandBox();
            case "docker":
                return new JavaDockerCodeSandBox();
            case "remote":
                return new RemoteCodeSandBox();
            default:
                return new ExampleCodeSandBox(); // 默认使用示例沙箱
        }
    }
}
