package space.kuikui.oj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import space.kuikui.oj.config.RabbitMqConfig;

import static space.kuikui.oj.config.RabbitMqConfig.initRabbitMqConfig;

/**
 * @author kuikui
 * @date 2025/3/15 17:56
 */
@EnableRabbit
@EnableScheduling
@SpringBootApplication
@MapperScan("space.kuikui.oj.mapper")
public class OjApplication {

    public static void main(String[] args) {
        // 初始启动消息队列
        initRabbitMqConfig();
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(OjApplication.class, args);
    }

}
