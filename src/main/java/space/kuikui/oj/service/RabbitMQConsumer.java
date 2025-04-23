package space.kuikui.oj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import space.kuikui.oj.judeg.Judeg;
import space.kuikui.oj.mapper.QuestionMapper;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.entity.Question;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author kuikui
 * @date 2025/4/17 14:36
 */
@Service
@Slf4j
public class RabbitMQConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private Judeg judeg;
    @Resource
    private QuestionMapper questionMapper;

    @RabbitListener(queues = {"code_queue"})
    public void consume(String message) throws IOException, InterruptedException {
        SubmitRequest submitRequest = objectMapper.readValue(message, SubmitRequest.class);
        log.info("用户选择的编程语言是："+submitRequest.getLanguage());
        Long questionId = submitRequest.getQuestionId();

        Question question = questionMapper.selectById(questionId);
        judeg.judgeAllTestCases(submitRequest.getCode(),question);
    }


}
