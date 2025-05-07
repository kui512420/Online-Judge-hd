package space.kuikui.oj.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

/**
 * @author kuikui
 * @date 2025/4/5 17:51
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;
    /**
     * 消耗内存
      */
    private Long memory;

    /**
     * 消耗时间
     */
    private Long time;
    /**
     * 用户输出
     */
    private String userOutput;

    /**
     * 是否通过
     * 0 未通过
     * 1 通过
     */
    private Integer passed;

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // 处理异常，这里简单返回一个空字符串，实际中可以根据需求调整
            return "";
        }
    }
}
