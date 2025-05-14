package space.kuikui.oj.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRank {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer submitCount;
    private Integer acceptCount;
}
