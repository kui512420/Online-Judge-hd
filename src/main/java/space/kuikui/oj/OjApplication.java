package space.kuikui.oj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import space.kuikui.oj.config.RabbitMqConfig;

import static space.kuikui.oj.config.RabbitMqConfig.initRabbitMqConfig;

@SpringBootApplication
@MapperScan("space.kuikui.oj.mapper")
public class OjApplication {

    public static void main(String[] args) {
        // 初始启动消息队列
        initRabbitMqConfig();
        SpringApplication.run(OjApplication.class, args);
    }

}
