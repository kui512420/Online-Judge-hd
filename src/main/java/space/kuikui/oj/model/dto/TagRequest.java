package space.kuikui.oj.model.dto;

import lombok.Data;

/**
 * @author kuikui
 * @date 2025/4/25 17:12
 */
@Data
public class TagRequest {
    private String tag;
    private Integer type;
    private Integer count;
    private Integer page;
}
