package space.kuikui.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import space.kuikui.oj.judge.codesandbox.CodeSandBox;
import space.kuikui.oj.judge.codesandbox.CodeSandBoxFactory;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeResponse;
import space.kuikui.oj.mapper.QuestionMapper;
import space.kuikui.oj.mapper.QuestionSubmitMapper;
import space.kuikui.oj.mapper.UserRankMapper;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.entity.JudgeInfo;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.QuestionSubmit;
import org.springframework.amqp.core.Message;
import space.kuikui.oj.model.entity.UserRank;

import java.io.IOException;
import java.util.List;

/**
 * 队列的消费者
 * @author kuikui
 * @date 2025/4/17 14:36
 */
@Service
@Slf4j
public class RabbitMQConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;
    @Resource
    private UserRankMapper userRankMapper;
    @Resource
    private QuestionMapper questionMapper;

    /**
     * 监听队列消息
     * @param message
     */
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    public void consume(Message  message, Channel channel) {
        String msg = new String(message.getBody());
        try {
            ExecuteCodeRequest executeCodeRequest = objectMapper.readValue(msg, ExecuteCodeRequest.class);
            // 使用远程代码沙箱
            CodeSandBox remoteSandBox = CodeSandBoxFactory.newInstance("remote");
            ExecuteCodeResponse response = remoteSandBox.executeCode( executeCodeRequest);

            List<JudgeInfo> judgeInfoList = response.getJudgeInfo();

            judgeInfoList.forEach(System.out::println);
            // 查询用户是否是首次通过

            Long questionId =  executeCodeRequest.getQuestion().getId();
            Long userId = executeCodeRequest.getUserId();
            Long submitId = executeCodeRequest.getQuestionSubmitId();
            QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("questionId",questionId)
                    .eq("userId",userId)
                                .eq("status", 2);
            boolean isFirst = questionSubmitMapper.selectCount(queryWrapper) == 0;
            System.out.println(response.getStatus()+"sdsd");
            if (response.getStatus() == 2) {
                // 更新执行的代码的状态
                if(isFirst){
                    // 更新执行的代码的通过数量
                    updateAcceptedCount(questionId);
                    UpdateWrapper<UserRank> updateWrapper2 = new UpdateWrapper<>();
                    updateWrapper2.eq("userId", userId);
                    updateWrapper2.setSql("acceptCount = acceptCount + 1");
                    userRankMapper.update(UserRank.builder().build(), updateWrapper2);
                }
                updateQuestionSubmitStatus(submitId,2);

            }else{
                System.out.println(submitId+"sdsd");
                updateQuestionSubmitStatus(submitId,3);
            }
            updateJudgeInfo(submitId,judgeInfoList);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("解析消息时出现异常", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 更新提交状态
     * @param submitId
     */
    private void updateQuestionSubmitStatus(Long submitId,Integer status) {
        try {
            UpdateWrapper<QuestionSubmit> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", submitId);
            QuestionSubmit updateQuestionSubmit = new QuestionSubmit();
            updateQuestionSubmit.setStatus(status);
            questionSubmitMapper.update(updateQuestionSubmit, updateWrapper);
        } catch (Exception e) {
            log.error("更新提交记录状态时出现异常，submitId: {}", submitId, e);
        }
    }

    /**
     * 更新通过数量
     * @param questionId
     */
    private void updateAcceptedCount(Long questionId) {
        try {
            UpdateWrapper<Question> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", questionId);
            updateWrapper.setSql("acceptedNum = acceptedNum + 1");
            questionMapper.update(Question.builder().build(), updateWrapper);
        } catch (Exception e) {
            log.error("更新通过数量时出现异常，questionId: {}", questionId, e);
        }
    }


    /**
     * 更新判题信息
     * @param questionSummitId
     */
    private void updateJudgeInfo(Long questionSummitId, List<JudgeInfo> judgeInfoList) {
        try {
            UpdateWrapper<QuestionSubmit> updateWrapper = new UpdateWrapper<>();
            QuestionSubmit questionSubmit = new QuestionSubmit();
            questionSubmit.setJudgeInfo(judgeInfoList.toString());
            updateWrapper.eq("id", questionSummitId);
            questionSubmitMapper.update(questionSubmit, updateWrapper);
        } catch (Exception e) {
            log.error("更新判题信息失败，questionSummitId: {}", questionSummitId, e);
        }
    }
}