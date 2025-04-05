package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.QuestionPostRequest;
import space.kuikui.oj.model.dto.QuestionRequest;
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
    public BaseResponse<Page<QuestionListVo>> questions(QuestionRequest questionRequest) {
        Page<QuestionListVo> pageInfo = questionService.selectAllQuestion(questionRequest);
        return ResultUtils.success("查询列表成功",pageInfo);
    }
    @GetMapping("/questionInfo/{id}")
    public BaseResponse<QuestionViewVo> questionInfo(@PathVariable("id") Long id) {
        QuestionViewVo pageInfo = questionService.findOne(id);
        return ResultUtils.success("获取题目信息成功",pageInfo);
    }
    @PostMapping("/question")
    public BaseResponse<Integer> question(@RequestHeader(value = "AccessToken",required = false) String accessToken,@RequestBody QuestionPostRequest questionPostRequest) {
        Long id = null;
        System.out.println(accessToken);
        try{
            JwtLoginUtils jwtLoginUtils = new JwtLoginUtils();
            id = (Long) jwtLoginUtils.jwtPeAccess(accessToken).get("id");
            questionPostRequest.setUserId(id);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.PARMS_ERROR,"令牌无效");
        }
        Integer count = questionService.put(questionPostRequest);
        return ResultUtils.success("新增题目成功",count);
    }
}
