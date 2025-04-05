package space.kuikui.oj.judeg.codesandbox;

import space.kuikui.oj.judeg.codesandbox.impl.ExampleCodeSandBox;

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
            default:
                return null;
        }
    }
}
