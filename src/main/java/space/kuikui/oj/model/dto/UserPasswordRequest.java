package space.kuikui.oj.model.dto;

import lombok.Data;

/**
 * @author kuikui
 * @date 2025/3/23 17:18
 */
@Data
public class UserPasswordRequest {
    private String usrePassword;
    private String newUserPassword;
    private String email;
    private String code;
}
