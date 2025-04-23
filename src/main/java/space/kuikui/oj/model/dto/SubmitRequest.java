package space.kuikui.oj.model.dto;

import lombok.Data;

/**
 * @author kuikui
 * @date 2025/4/10 23:33
 */
@Data
public class SubmitRequest {
    private long userId;
    private String language;
    private String code;
    private long questionId;
}
