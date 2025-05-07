package space.kuikui.oj.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.judeg.Judeg;
import space.kuikui.oj.model.dto.SubmitListRequest;
import space.kuikui.oj.model.dto.SubmitRankRequest;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.entity.QuestionSubmit;
import space.kuikui.oj.service.QuestionService;
import space.kuikui.oj.service.QuestionSubmitService;
import space.kuikui.oj.service.RabbitMQProducer;

import java.io.File;

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
    private static final String TEMP_PATH = System.getProperty("user.dir") + File.separator + "temp";
    /**
     * 提交检测代码
     * @param submitRequest
     * @param token
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/sub")
    public BaseResponse<String> submitQuestion(@RequestBody SubmitRequest submitRequest, @RequestHeader(value = "Accesstoken",required = false) String token) throws JsonProcessingException {
        Long id = null;
        String userName = "";
        try {
            // 提交者的 id 和 名称
            id = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(token).get("id")) ;
            userName = (String) jwtLoginUtils.jwtPeAccess(token).get("userName");
        }catch (Exception e) {
            e.printStackTrace();
        }
        submitRequest.setUserId(id);
        submitRequest.setUserName(userName);
        // 保存 用户提交的信息
        Long subId = questionSubmitService.submit(submitRequest);
        submitRequest.setId(subId);
        // 添加 判题任务 到队列
        rabbitMQProducer.sendMessage("code_exchange","routingkey", submitRequest);
        return ResultUtils.success("提交成功","");
    }

    /**
     * 获取 代码沙箱执行后的列表
     * @param submitListRequest
     * @param token
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<Page<QuestionSubmit>> submitQuestionList(@RequestBody SubmitListRequest submitListRequest, @RequestHeader(value = "Accesstoken",required = false) String token) {
        Long id = null;
        try {
            id = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(token).get("id")) ;
        }catch (Exception e) {
            e.printStackTrace();
        }
        submitListRequest.setUserId(id);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.list(submitListRequest);
        return ResultUtils.success("获取成功",questionSubmitPage);
    }

    /**
     * 获取 代码沙箱执行后的列表
     * @param submitListRequest
     * @param token
     * @return
     */
    @PostMapping("/rank")
    public BaseResponse<Page<QuestionSubmit>> submitQuestionRankList(@RequestBody SubmitRankRequest submitRankRequest) {

        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.rank(submitRankRequest);
        return ResultUtils.success("获取成功",questionSubmitPage);
    }
    /**
     * 定时任务：每半个小时删除代码文件
     *
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void updateCompetitionStatus() {
        FileUtil.del(TEMP_PATH);
    }
}
