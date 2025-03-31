package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.model.vo.QuestionListVo;
import space.kuikui.oj.model.vo.QuestionViewVo;
import space.kuikui.oj.service.QuestionService;

import javax.annotation.Resource;

/**
 * @author kuikui
 * @date 2025/3/16 19:10
 */
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @GetMapping("/questions")
    public BaseResponse<Page<QuestionListVo>> questions(@Param("pageNow") Integer pageNow, @Param("pageSize") Integer pageSize) {
        Page<QuestionListVo> pageInfo = questionService.selectAllQuestion(pageNow,pageSize);
        return ResultUtils.success("查询列表成功",pageInfo);
    }
    @GetMapping("/questionInfo")
    public BaseResponse<QuestionViewVo> questionInfo(@Param("id") Integer id) {
        QuestionViewVo pageInfo = questionService.findOne(id);
        return ResultUtils.success("获取题目信息成功",pageInfo);
    }
}
