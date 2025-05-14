package space.kuikui.oj.model.dto;

import lombok.Data;

/**
 * @author kuikui
 * @date 2025/4/11 11:12
 */
@Data
public class UserInfoRequest {
    private Long id;
    private String userName;
    private String password;
    private String email;
    private String userRole;
}
