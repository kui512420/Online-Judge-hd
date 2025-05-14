package space.kuikui.oj.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * @author kuikui
 * @date 2025/4/10 23:33
 */
@Data
public class SubmitRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long userId;
    private String userName;
    private String language;
    private String code;
    private long questionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long competitionId;
}
