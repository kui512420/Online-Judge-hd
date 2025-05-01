package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.QuestionPostRequest;
import space.kuikui.oj.model.dto.QuestionRequest;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.vo.QuestionListVo;
import space.kuikui.oj.model.vo.QuestionViewVo;
import space.kuikui.oj.service.QuestionService;
import space.kuikui.oj.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 30767
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-03-16 20:33:35
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;
    private Question question;

    @Override
    public Page<QuestionListVo> selectAllQuestion(QuestionRequest questionRequest) {
        Integer pageNow = questionRequest.getPageNow();
        Integer pageSize = questionRequest.getPageSize();
        Integer findType = questionRequest.getFindType();

        // 创建分页对象
        Page<Question> page = new Page<>(pageNow, pageSize);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();

        // 构建查询条件
        switch (findType) {
            case 0: // 查询所有
                break;
            case 1: // 按ID查询
                queryWrapper.eq("id", questionRequest.getId());
                break;
            case 2: // 按标题查询
                queryWrapper.like("title", questionRequest.getQuestionName());
                break;
            case 3: // 按标签查询
                List<String> tags = questionRequest.getTags();
                if (tags != null && !tags.isEmpty()) {
                    for (String tag : tags) {
                        tag = tag.replaceAll("[\\[\\]\"]", "");
                        queryWrapper.like("tags", "\"" + tag + "\"");
                    }
                }
                break;
            case 4: // 同时按标题和标签查询
                Integer submitType = questionRequest.getSubmitNumOrderType();
                if (submitType != null) {
                    if(questionRequest.getSubmitNumOrderType()==1){
                        queryWrapper.orderByDesc("submitNum");
                    }else if(questionRequest.getSubmitNumOrderType()==0){
                        queryWrapper.orderByAsc("submitNum");
                    }
                }
                List<String> combinedTags = questionRequest.getTags();
                if (combinedTags.size()>=1) {
                    // 处理单个或多个 tags 的情况
                    if (combinedTags.size() == 1) {
                        // 单个 tag 精确匹配
                        String tag = combinedTags.get(0).replaceAll("[\\[\\]\"]", "");
                        queryWrapper.like("tags", tag);
                    } else {
                        queryWrapper.and(wrapper -> {
                            for (String tag : combinedTags) {
                                tag = tag.replaceAll("[\\[\\]\"]", "");
                                wrapper.or().like("tags", tag);
                            }
                        });
                    }
                }
                break;
            default:
                throw new BusinessException(ErrorCode.PARMS_ERROR,"findType 参数错误");
        }

        // 执行查询
        Page<Question> questionPage = questionMapper.selectPage(page, queryWrapper);

        // 转换为VO的分页对象
        Page<QuestionListVo> voPage = new Page<>(pageNow, pageSize);
        voPage.setTotal(questionPage.getTotal());

        // 转换列表内容
        List<QuestionListVo> records = questionPage.getRecords().stream()
                .map(QuestionListVo::new)
                .collect(Collectors.toList());

        voPage.setRecords(records);
        return voPage;
    }
    @Override
    public QuestionViewVo findOne(Long id) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        Question question = questionMapper.selectOne(queryWrapper);
        QuestionViewVo questionListVo = new QuestionViewVo(question);
        return questionListVo;
    }

    @Override
    public Integer put(QuestionPostRequest questionPostRequest) {
        Question question = Question.builder()
                .content(questionPostRequest.getContent())
                        .judgeConfig(questionPostRequest.getJudgeConfig())
                                .title(questionPostRequest.getTitle())
                                        .tags(questionPostRequest.getTags())
                                                .judgeCase(questionPostRequest.getJudgeCase())
                .userId(questionPostRequest.getUserId())
                .build();
        return questionMapper.insert(question);
    }

    @Override
    public void submit() {

    }

    @Override
    public Question findDetail(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public Integer updateInfo(QuestionPostRequest questionPostRequest) {
        return questionMapper.updateQuestion(questionPostRequest);
    }

    @Override
    public List<Question> queryQuestions() {
        return questionMapper.selectList(null);
    }
}




