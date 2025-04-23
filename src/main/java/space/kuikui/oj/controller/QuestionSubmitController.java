package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.judeg.Judeg;
import space.kuikui.oj.model.dto.SubmitListRequest;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.entity.QuestionSubmit;
import space.kuikui.oj.service.QuestionService;
import space.kuikui.oj.service.QuestionSubmitService;
import space.kuikui.oj.service.RabbitMQProducer;

/**
 * @author kuikui
 * @date 2025/4/10 23:31
 */
@RestController
@RequestMapping("/api/submit")
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private JwtLoginUtils jwtLoginUtils;
    @Resource
    private RabbitMQProducer rabbitMQProducer;


    @PostMapping("/sub")
    public BaseResponse<String> submitQuestion(@RequestBody SubmitRequest submitRequest, @RequestHeader(value = "Accesstoken",required = false) String token) throws JsonProcessingException {
        Long id = null;
        try {
            id = (Long) jwtLoginUtils.jwtPeAccess(token).get("id");
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(submitRequest.getQuestionId());
        submitRequest.setUserId(id);
        rabbitMQProducer.sendMessage("code_exchange","routingkey", submitRequest);
        int count = questionSubmitService.submit(submitRequest);
        return ResultUtils.success("提交成功","");
    }
    @PostMapping("/list")
    public BaseResponse<Page<QuestionSubmit>> submitQuestionList(@RequestBody SubmitListRequest submitListRequest, @RequestHeader(value = "Accesstoken",required = false) String token) {
        Long id = null;
        try {
            id = (Long) jwtLoginUtils.jwtPeAccess(token).get("id");
        }catch (Exception e) {
            e.printStackTrace();
        }
        submitListRequest.setUserId(id);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.list(submitListRequest);
        return ResultUtils.success("获取成功",questionSubmitPage);
    }
}
