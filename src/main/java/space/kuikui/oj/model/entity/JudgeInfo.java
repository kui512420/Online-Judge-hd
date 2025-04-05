package space.kuikui.oj.model.entity;

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

}
