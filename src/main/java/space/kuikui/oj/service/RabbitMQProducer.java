package space.kuikui.oj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import space.kuikui.oj.model.entity.QuestionSubmit;

/**
 * @author kuikui
 * @date 2025/4/17 14:32
 */
@Service
@Slf4j
public class RabbitMQProducer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private RabbitTemplate rabbitTemplate;
    public void sendMessage(String exchange,String routingKey,Object message) throws JsonProcessingException {

        String objString = objectMapper.writeValueAsString(message);
        rabbitTemplate.convertAndSend(exchange,routingKey,objString);
        log.info("Sent message: " + message);
    }
}
