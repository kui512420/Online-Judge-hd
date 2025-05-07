package space.kuikui.oj.service;

import space.kuikui.oj.model.entity.CompetitionQuestion;
import space.kuikui.oj.model.vo.QuestionListVo;

import java.util.List;

public interface CompetitionQuestionService {
     public List<QuestionListVo> getQuestions(Long id);
}
