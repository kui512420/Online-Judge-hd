package space.kuikui.oj.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author kuikui
 * @date 2025/4/17 14:17
 */
@Slf4j
public class RabbitMqConfig {
    private static final String EXCHANGE_NAME = "code_exchange";
    private static final String QUEUE_NAME = "code_queue";
    private static final String ROUTINGKEY = "routingkey";
    public static void initRabbitMqConfig() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 创建一个队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTINGKEY);
            log.info("消息队列启动成功");
        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
