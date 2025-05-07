package space.kuikui.oj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import space.kuikui.oj.service.UserRankService;

@Component
@EnableScheduling
public class RankingScheduler implements CommandLineRunner {

    @Autowired
    private UserRankService rankingService;

    @Override
    public void run(String... args) throws Exception {
        // 在Spring Boot启动时更新排行榜数据
        rankingService.updateRankingData();
    }

    // 每半小时更新一次排行榜数据
    @Scheduled(fixedRate = 1800000)
    public void scheduledUpdate() {
        rankingService.updateRankingData();
    }
}