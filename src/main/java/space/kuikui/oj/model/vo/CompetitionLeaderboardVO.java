package space.kuikui.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 竞赛排行榜数据VO
 * @author kuikui
 * @date 2025/5/12 10:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionLeaderboardVO {
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 得分
     */
    private Integer score;
    
    /**
     * 提交数
     */
    private Integer submitCount;
    
    /**
     * 通过数
     */
    private Integer acceptCount;
    
    /**
     * 参与时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 是否已交卷
     */
    private Integer isSubmitted;
    
    /**
     * 通过的测试用例数量
     */
    private Integer passedTestCases;
} 