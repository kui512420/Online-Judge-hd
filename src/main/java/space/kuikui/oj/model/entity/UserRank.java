package space.kuikui.oj.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRank {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer submitCount;
    private Integer acceptCount;
}
