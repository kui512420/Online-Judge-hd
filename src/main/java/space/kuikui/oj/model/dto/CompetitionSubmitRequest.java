package space.kuikui.oj.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Map;

/**
 * 竞赛提交请求DTO
 * @author kuikui
 * @date 2025/5/12 22:22
 */
@Data
public class CompetitionSubmitRequest {
    /**
     * 竞赛ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String competitionId;
    
    /**
     * 题目提交信息，key为题目ID，value为提交信息
     */
    private Map<String, QuestionSubmissionInfo> questionSubmissions;
    
    /**
     * 最后保存时间
     */
    private String lastSavedAt;
    
    /**
     * 题目提交信息内部类
     */
    @Data
    public static class QuestionSubmissionInfo {
        /**
         * 代码
         */
        private String code;
        
        /**
         * 编程语言
         */
        private String language;
        
        /**
         * 题目ID
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private String questionId;
    }
} 