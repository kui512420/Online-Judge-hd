package space.kuikui.oj.model.dto;

import lombok.Data;

/**
 * @author kuikui
 * @date 2025/3/27 14:24
 */
@Data
public class UserListRequest {

    private int page;
    private int size;
    /**
     * 0 全部查询
     * 1 通过id查询
     * 2 通过账号查询
     * 3 通过邮箱查询
     */
    private int type;

}
