package space.kuikui.oj.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * @author kuikui
 * @date 2025/4/11 9:05
 */
@Data
public class SubmitListRequest {
    private int page;
    private int size;
    /**
     * 0 全部查询
     * 1 通过id查询
     * 2 通过用户ID查询
     */
    private Integer type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long id;
    private long userId;
    private long questionId;
}
