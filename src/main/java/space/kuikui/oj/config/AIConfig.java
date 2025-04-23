package space.kuikui.oj.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kuikui
 * @date 2025/4/8 11:11
 */
@Configuration
public class AIConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        String example = "{\"title\": \"题目2\", \"content\": \"000\", \"tags\": \"[\"测试\", \"题目\"]\", \"judgeCase\": \"[{\\\"input\\\": \\\"1 2\\\", \\\"output\\\": \\\"3\\\"}]\", \"judgeConfig\": \"[{\\\"input\\\": \\\"1 2\\\", \\\"output\\\": \\\"3\\\"}]\"}";
        return builder.defaultSystem("你是一个算法出题专家,题目要是中文， 你能规范的写出一些OJ算法题目和对应的测试用例,输出格式是"+example+",题目要是中文").build();
    }
}
