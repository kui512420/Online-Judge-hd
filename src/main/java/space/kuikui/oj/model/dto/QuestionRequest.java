package space.kuikui.oj.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

/**
 * @author kuikui
 * @date 2025/4/5 15:28
 */
@Data
public class QuestionRequest {



    private String questionName;
    /**
     * 0 查询所有
     * 1 通过id查询
     * 2 通过标题查询
     * 3 通过标签数组查询
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private Integer findType;
    private Integer pageNow;
    private Integer pageSize;
    private List<String> tags;
    private Integer submitNumOrderType;
}
