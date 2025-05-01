package space.kuikui.oj.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class LoginLogRequest {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;
}