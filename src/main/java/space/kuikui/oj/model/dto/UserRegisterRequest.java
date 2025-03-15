package space.kuikui.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kuikui
 * @date 2025/3/15 16:15
 */
@Data
public class UserRegisterRequest implements Serializable {
    private String userAccount;
    private String userPassword;
    private String userCheakPassword;
    private String email;
    private String emailCode;
}

