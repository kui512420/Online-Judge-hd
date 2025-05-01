package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.kuikui.oj.mapper.QuestionMapper;
import space.kuikui.oj.mapper.QuestionSubmitMapper;
import space.kuikui.oj.model.dto.SubmitListRequest;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.QuestionSubmit;
import space.kuikui.oj.service.QuestionSubmitService;


/**
 * @author kuikui
 * @date 2025/4/10 23:36
 */
@Service
public class QuestionSubmitServiceImpl implements QuestionSubmitService {

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;
    @Resource
    private QuestionMapper questionMapper;

    /**
     * @todo 插入提交信息
     * @param submitRequest
     * @return
     */
    @Override
    public Long submit(SubmitRequest submitRequest) {
        String language = submitRequest.getLanguage();
        String code = submitRequest.getCode();
        long questionId = submitRequest.getQuestionId();
        long userId = submitRequest.getUserId();
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setLanguage(language);
        questionSubmit.setUserId(userId);
        questionSubmit.setCode(code);
        questionSubmit.setQuestionId(questionId);
        questionSubmitMapper.insert(questionSubmit);

        // 查询用户是否是首次提交
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId)
                .eq("userId", userId);
        boolean exists = questionSubmitMapper.selectCount(queryWrapper) > 0;
        if(!exists){
            UpdateWrapper<Question> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", questionId);
            updateWrapper.setSql("submitNum = submitNum + 1");
            questionMapper.update(Question.builder().build(), updateWrapper);
        }
        return questionSubmit.getId();
    }

    /**
     * @todo 查询提交信息
     * @param submitListRequest
     * @return
     */
    @Override
    public Page<QuestionSubmit> list(SubmitListRequest submitListRequest) {
        long subId = submitListRequest.getId();
        long userId = submitListRequest.getUserId();
        Integer type = submitListRequest.getType();
        Page<QuestionSubmit> page = new Page<>(submitListRequest.getPage(), submitListRequest.getSize());

        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();

        switch (type) {
            case 1: // 通过提交ID查询
                queryWrapper.eq("id", subId);
                break;
            case 2: // 通过用户ID查询
                queryWrapper.eq("userId", userId);
                break;
            case 0: // 默认查询所有
                break;
            case 3: // 查询用户某他题的提交记录
                queryWrapper.eq("questionId",submitListRequest.getQuestionId() );
                queryWrapper.eq("userId", userId);
            default:
                // 不添加特定条件，查询所有
                break;
        }

        // 添加排序条件，按提交时间降序排列
        queryWrapper.orderByDesc("createTime");

        return questionSubmitMapper.selectPage(page, queryWrapper);
    }
}
