package space.kuikui.oj.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kuikui
 * @date 2025/3/15 17:48
 */
@Data
public class UserRegisterVo implements Serializable {
    private long id;
    private String userAccount;
    private String email;
}
