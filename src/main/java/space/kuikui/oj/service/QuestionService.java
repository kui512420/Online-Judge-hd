package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import space.kuikui.oj.model.dto.QuestionPostRequest;
import space.kuikui.oj.model.dto.QuestionRequest;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.model.vo.QuestionListVo;
import space.kuikui.oj.model.vo.QuestionViewVo;

import java.util.List;

/**
* @author 30767
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2025-03-16 20:33:35
*/
public interface QuestionService extends IService<Question> {
    Page<QuestionListVo> selectAllQuestion(QuestionRequest questionRequest);
    QuestionViewVo findOne(Long id);
    Integer put(QuestionPostRequest questionPostRequest);

    void submit();

    Question findDetail(Long id);

    Integer updateInfo(QuestionPostRequest questionPostRequest);

    List<Question> queryQuestions();
}
