package space.kuikui.oj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.judeg.codesandbox.CodeSandBox;
import space.kuikui.oj.judeg.codesandbox.CodeSandBoxFactory;
import space.kuikui.oj.judeg.codesandbox.CodeSandBoxProxy;
import space.kuikui.oj.judeg.codesandbox.impl.ExampleCodeSandBox;
import space.kuikui.oj.judeg.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.service.RabbitMQProducer;

import java.util.Collections;import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static space.kuikui.oj.config.RabbitMqConfig.initRabbitMqConfig;

@SpringBootTest
class OjApplicationTests {

    @Resource
    private JwtLoginUtils jwtLoginUtils;
    @Resource
    private RabbitMQProducer rabbitMQProducer;

    @Value("${code.default}")
    private String type;

    @Test
    void jwtTest() {
        User user = new User();
        user.setId(423423432l);
        user.setUserAccount("sdasdasdas");
        //设token
        String token = jwtLoginUtils.jwtBdAccess(user);
        //校验token
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(token);
        System.out.println(map.get("id"));
    }

    @Test
    void codeSandBoxTest() {
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        codeSandBox = new CodeSandBoxProxy(codeSandBox);
        String code = "";
        List<String> info = Arrays.asList("12 10","1 0");
        ExecuteCodeRequest codeRequest = ExecuteCodeRequest.builder()
                        .code(code)
                        .language("java")
                        .inputList(info)
                        .build();
        codeSandBox.executeCode(codeRequest);
    }
    @Test
    void RabbitMqTest()  {
        // 初始启动消息队列
        initRabbitMqConfig();
    }
}
