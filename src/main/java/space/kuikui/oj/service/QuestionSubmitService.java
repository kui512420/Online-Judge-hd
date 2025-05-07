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
}
