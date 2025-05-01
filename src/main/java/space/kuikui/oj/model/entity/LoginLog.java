package space.kuikui.oj.model.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author kuikui
 * @date 2025/4/25 9:55
 */
@Data
@Builder
public class LoginLog {
    private Long id;
    private String user;
    private String ip;
    private String device;
    private Date loginTime;
    private Integer status;
    private String errorMsg;
}
