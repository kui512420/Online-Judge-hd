package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import space.kuikui.oj.mapper.CompetitionQuestionMapper;
import space.kuikui.oj.mapper.QuestionMapper;
import space.kuikui.oj.model.entity.CompetitionQuestion;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.vo.QuestionListVo;
import space.kuikui.oj.service.CompetitionQuestionService;

import java.util.ArrayList;
import java.util.List;

@Service
public  class CompetitionQuestionServiceImpl implements CompetitionQuestionService {


    @Resource
    private CompetitionQuestionMapper competitionQuestionMapper;
    @Resource
    private QuestionMapper questionMapper;

    public List<QuestionListVo> getQuestions(Long id){
        QueryWrapper<CompetitionQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("competition_id", id);
        List<QuestionListVo> questionListVos = new ArrayList<>();
        List<CompetitionQuestion> competitionQuestions = competitionQuestionMapper.selectList(queryWrapper);
        for (CompetitionQuestion competitionQuestion : competitionQuestions) {
            Question question = questionMapper.selectById(competitionQuestion.getQuestionId());
            questionListVos.add(new QuestionListVo(question));
        }
        return questionListVos;
    }
}
