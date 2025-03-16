package space.kuikui.oj.model.dto;

import lombok.Data;

/**
 * @author kuikui
 * @date 2025/3/16 22:33
 */
@Data
public class UserLoginRequset {
    private String user;
    private String password;
    private String code;
}
