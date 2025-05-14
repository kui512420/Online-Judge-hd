package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import space.kuikui.oj.model.dto.SubmitListRequest;
import space.kuikui.oj.model.dto.SubmitRankRequest;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.dto.UserCommitRequest;
import space.kuikui.oj.model.entity.QuestionSubmit;

import java.util.List;

/**
 * @author kuikui
 * @date 2025/4/10 23:36
 */
public interface QuestionSubmitService {
    Long submit(SubmitRequest submitRequest);

    Page<QuestionSubmit> list(SubmitListRequest submitListRequest);

    UserCommitRequest userCommitInfo(Long userId);

    Page<QuestionSubmit> rank(SubmitRankRequest submitRankRequest);
    
    /**
     * 获取用户的所有提交记录（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的用户提交记录列表
     */
    Page<QuestionSubmit> getAllUserSubmissions(Long userId, int pageNum, int pageSize);
}
