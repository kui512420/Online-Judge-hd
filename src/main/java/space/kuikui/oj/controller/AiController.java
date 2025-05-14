package space.kuikui.oj.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.model.dto.QuestionConfingRequest;
import space.kuikui.oj.service.RabbitMQProducer;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class AiController {
    private final ChatClient chatClient;
    // 通过构造函数注入 ChatClient
    public AiController(ChatClient chatClient, OpenAiChatModel chatModel) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
    }
    @Resource
    private RabbitMQProducer rabbitMQProducer;
    private final OpenAiChatModel chatModel;
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @PostMapping("/ai")
    public BaseResponse<String> ai(@RequestBody QuestionConfingRequest questionConfingRequest) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("生成 ").append("题目描述是").append(questionConfingRequest.getQuestionType())
                .append("，题目难度是 ").append(questionConfingRequest.getQuestionDifficulty()).append("，测试用例条数：").append(questionConfingRequest.getQuestionCount());
        System.out.println(promptBuilder.toString());
        String response = chatClient.prompt()
                .user(promptBuilder.toString())
                .call()
                .content();
        return ResultUtils.success("生成成功",response);
    }
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chat(@RequestBody String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
    
    /**
     * 获取deepseek API余额
     * @return API余额信息
     */
    @GetMapping("/ai/balance")
    public BaseResponse<Map<String, Object>> getApiBalance() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.deepseek.com/user/balance",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return ResultUtils.success("获取API余额成功", response.getBody());
        } catch (Exception e) {
            return ResultUtils.error(500, "获取API余额失败: " + e.getMessage(), null);
        }
    }
}