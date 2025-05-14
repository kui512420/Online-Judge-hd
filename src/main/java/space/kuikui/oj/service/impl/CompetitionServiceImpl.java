package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.mapper.CompetitionMapper;
import space.kuikui.oj.mapper.CompetitionQuestionMapper;
import space.kuikui.oj.mapper.QuestionMapper;
import space.kuikui.oj.mapper.UserMapper;
import space.kuikui.oj.mapper.CompetitionParticipantMapper;
import space.kuikui.oj.mapper.QuestionSubmitMapper;
import space.kuikui.oj.model.dto.CompetitionAddRequest;
import space.kuikui.oj.model.dto.CompetitionRequest;
import space.kuikui.oj.model.dto.CompetitionSubmitRequest;
import space.kuikui.oj.model.dto.SubmitRequest;
import space.kuikui.oj.model.entity.Competition;
import space.kuikui.oj.model.entity.CompetitionQuestion;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.model.entity.CompetitionParticipant;
import space.kuikui.oj.model.entity.QuestionSubmit;
import space.kuikui.oj.model.vo.CompetitionVO;
import space.kuikui.oj.model.vo.CompetitionLeaderboardVO;
import space.kuikui.oj.service.CompetitionService;
import space.kuikui.oj.service.QuestionSubmitService;
import space.kuikui.oj.judge.codesandbox.model.ExecuteCodeRequest;
import space.kuikui.oj.service.RabbitMQProducer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 竞赛服务实现
 * @author kuikui
 * @date 2025/4/28 16:05
 */
@Service
@Slf4j
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements CompetitionService {

    @Resource
    private CompetitionMapper competitionMapper;
    
    @Resource
    private CompetitionQuestionMapper competitionQuestionMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private QuestionMapper questionMapper;
    
    @Resource
    private CompetitionParticipantMapper competitionParticipantMapper;
    
    @Resource
    private QuestionSubmitMapper questionSubmitMapper;
    
    @Resource
    private QuestionSubmitService questionSubmitService;
    
    @Resource
    private RabbitMQProducer rabbitMQProducer;
    
    @Override
    public Page<CompetitionVO> pageCompetitions(CompetitionRequest competitionRequest) {
        if (competitionRequest == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        // 构建查询条件
        QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        // 添加条件查询
        String name = competitionRequest.getName();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        
        Integer status = competitionRequest.getStatus();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        Integer creatorId = competitionRequest.getCreatorId();
        if (creatorId != null) {
            queryWrapper.eq("creator_id", creatorId);
        }
        
        LocalDateTime startTimeBegin = competitionRequest.getStartTimeBegin();
        if (startTimeBegin != null) {
            queryWrapper.ge("start_time", startTimeBegin);
        }
        
        LocalDateTime startTimeEnd = competitionRequest.getStartTimeEnd();
        if (startTimeEnd != null) {
            queryWrapper.le("start_time", startTimeEnd);
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc("create_time");
        
        // 分页查询
        Page<Competition> page = new Page<>(competitionRequest.getCurrent(), competitionRequest.getPageSize());
        Page<Competition> competitionPage = competitionMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        Page<CompetitionVO> voPage = new Page<>(competitionPage.getCurrent(), competitionPage.getSize(), competitionPage.getTotal());
        List<CompetitionVO> voList = competitionPage.getRecords().stream()
                .map(this::competitionToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCompetition(CompetitionAddRequest addRequest, Long creatorId) {
        if (addRequest == null || creatorId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 参数校验
            String name = addRequest.getName();
            if (StringUtils.isBlank(name)) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛名称不能为空");
            }
            
            LocalDateTime startTime = addRequest.getStartTime();
            LocalDateTime endTime = addRequest.getEndTime();
            if (startTime == null || endTime == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "开始时间或结束时间不能为空");
            }
            
            if (endTime.isBefore(startTime)) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "结束时间不能早于开始时间");
            }
            
            List<Long> questionIds = addRequest.getQuestionIds();
            List<Integer> scores = addRequest.getScores();
            if (questionIds == null || questionIds.isEmpty()) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛题目不能为空");
            }
            
            if (scores != null && scores.size() != questionIds.size()) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "题目分值数量与题目数量不匹配");
            }
            
            // 验证题目是否存在
            for (Long questionId : questionIds) {
                Question question = questionMapper.selectById(questionId);
                if (question == null) {
                    throw new BusinessException(ErrorCode.PARMS_ERROR, "题目ID " + questionId + " 不存在");
                }
            }
            
            // 创建竞赛
            Competition competition = new Competition();
            competition.setName(name);
            competition.setCreatorId(creatorId);
            competition.setStartTime(startTime);
            competition.setEndTime(endTime);
            competition.setDescription(addRequest.getDescription());
            
            // 设置状态
            LocalDateTime now = LocalDateTime.now();
            System.out.println(now+"-");
            System.out.println(startTime+"-");
            System.out.println(endTime+"-");
            if (now.isAfter(endTime)) {
                competition.setStatus(2); // 已结束
                System.out.println("状态: 已结束");
            } else if (now.isAfter(startTime)) {
                competition.setStatus(1); // 进行中
                System.out.println("状态: 进行中");
            } else {
                competition.setStatus(0); // 未开始
                System.out.println("状态: 未开始");
            }
            
            competition.setCreateTime(now);
            competition.setUpdateTime(now);
            competition.setIsDeleted(0);
            
            // 保存竞赛

            boolean saved = competitionMapper.insert(competition) == 1;
            if (!saved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加竞赛失败");
            }
            
            // 添加竞赛题目关联
            List<CompetitionQuestion> competitionQuestions = new ArrayList<>();
            for (int i = 0; i < questionIds.size(); i++) {
                CompetitionQuestion competitionQuestion = new CompetitionQuestion();
                competitionQuestion.setCompetitionId(competition.getId());
                competitionQuestion.setQuestionId(questionIds.get(i));
                
                // 设置分值，如果没有提供分值则默认100分
                if (scores != null && scores.size() > i) {
                    competitionQuestion.setScore(scores.get(i));
                } else {
                    competitionQuestion.setScore(100);
                }
                
                competitionQuestion.setCreateTime(now);
                competitionQuestion.setUpdateTime(now);
                competitionQuestions.add(competitionQuestion);
            }
            
            // 批量保存竞赛题目关联
            for (CompetitionQuestion competitionQuestion : competitionQuestions) {
                competitionQuestionMapper.insert(competitionQuestion);
            }
            
            return competition.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加竞赛失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加竞赛失败：" + e.getMessage());
        }
    }
    
    @Override
    public CompetitionVO getCompetitionDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        Competition competition = this.getById(id);
        if (competition == null || competition.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
        }
        
        return competitionToVO(competition);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCompetition(CompetitionAddRequest updateRequest, Long userId) {
        if (updateRequest == null || updateRequest.getId() == null || userId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 参数校验
            String name = updateRequest.getName();
            if (StringUtils.isBlank(name)) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛名称不能为空");
            }
            
            LocalDateTime startTime = updateRequest.getStartTime();
            LocalDateTime endTime = updateRequest.getEndTime();
            if (startTime == null || endTime == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "开始时间或结束时间不能为空");
            }
            
            if (endTime.isBefore(startTime)) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "结束时间不能早于开始时间");
            }
            
            List<Long> questionIds = updateRequest.getQuestionIds();
            List<Integer> scores = updateRequest.getScores();
            if (questionIds == null || questionIds.isEmpty()) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛题目不能为空");
            }
            
            if (scores != null && scores.size() != questionIds.size()) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "题目分值数量与题目数量不匹配");
            }
            
            // 验证题目是否存在
            for (Long questionId : questionIds) {
                Question question = questionMapper.selectById(questionId);
                if (question == null) {
                    throw new BusinessException(ErrorCode.PARMS_ERROR, "题目ID " + questionId + " 不存在");
                }
            }
            
            // 获取现有竞赛
            Competition competition = this.getById(updateRequest.getId());
            if (competition == null || competition.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 更新竞赛信息
            competition.setName(name);
            competition.setStartTime(startTime);
            competition.setEndTime(endTime);
            competition.setDescription(updateRequest.getDescription());
            
            // 更新状态
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(endTime)) {
                competition.setStatus(2); // 已结束
            } else if (now.isAfter(startTime)) {
                competition.setStatus(1); // 进行中
            } else {
                competition.setStatus(0); // 未开始
            }
            
            competition.setUpdateTime(now);
            
            // 保存竞赛
            boolean updated = this.updateById(competition);
            if (!updated) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新竞赛失败");
            }
            
            // 删除原有竞赛题目关联
            QueryWrapper<CompetitionQuestion> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("competition_id", competition.getId());
            competitionQuestionMapper.delete(queryWrapper);
            
            // 添加新的竞赛题目关联
            List<CompetitionQuestion> competitionQuestions = new ArrayList<>();
            for (int i = 0; i < questionIds.size(); i++) {
                CompetitionQuestion competitionQuestion = new CompetitionQuestion();
                competitionQuestion.setCompetitionId(competition.getId());
                competitionQuestion.setQuestionId(questionIds.get(i));
                
                // 设置分值，如果没有提供分值则默认100分
                if (scores != null && scores.size() > i) {
                    competitionQuestion.setScore(scores.get(i));
                } else {
                    competitionQuestion.setScore(100);
                }
                
                competitionQuestion.setCreateTime(now);
                competitionQuestion.setUpdateTime(now);
                competitionQuestions.add(competitionQuestion);
            }
            
            // 批量保存竞赛题目关联
            for (CompetitionQuestion competitionQuestion : competitionQuestions) {
                competitionQuestionMapper.insert(competitionQuestion);
            }
            
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新竞赛失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新竞赛失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompetitionStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // 查询所有未删除的竞赛
            QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_deleted", 0);
            List<Competition> competitions = this.list(queryWrapper);
            
            for (Competition competition : competitions) {
                Integer status = competition.getStatus();
                LocalDateTime startTime = competition.getStartTime();
                LocalDateTime endTime = competition.getEndTime();
                
                // 未开始 -> 进行中
                if (status == 0 && now.isAfter(startTime) && now.isBefore(endTime)) {
                    competition.setStatus(1);
                    competition.setUpdateTime(now);
                    this.updateById(competition);
                }
                // 进行中 -> 已结束
                else if (status == 1 && now.isAfter(endTime)) {
                    competition.setStatus(2);
                    competition.setUpdateTime(now);
                    this.updateById(competition);
                }
            }
        } catch (Exception e) {
            log.error("定时更新竞赛状态失败", e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinCompetition(Long competitionId, Long userId) {
        if (competitionId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 查询竞赛是否存在
            Competition competition = this.getById(competitionId);
            if (competition == null || competition.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 检查竞赛状态
            if (competition.getStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始或已结束，无法参与");
            }
            
            // 检查用户是否已参与过该竞赛
            QueryWrapper<CompetitionParticipant> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("competition_id", competitionId);
            queryWrapper.eq("user_id", userId);
            CompetitionParticipant existParticipant = competitionParticipantMapper.selectOne(queryWrapper);
            
            // 如果用户已参与过，直接返回成功
            if (existParticipant != null) {
                return true;
            }
            
            // 创建新的参与记录
            CompetitionParticipant participant = new CompetitionParticipant();
            participant.setCompetitionId(competitionId );
            participant.setUserId(userId );
            participant.setJoinTime(LocalDateTime.now());
            participant.setScore(0); // 初始分数为0
            participant.setRank(0);  // 初始排名为0
            participant.setCreateTime(LocalDateTime.now());
            participant.setUpdateTime(LocalDateTime.now());
            
            // 保存参与记录
            int inserted = competitionParticipantMapper.insert(participant);
            return inserted > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("参与竞赛失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参与竞赛失败：" + e.getMessage());
        }
    }
    
    /**
     * 提交竞赛答案
     * @param submitRequest 提交请求
     * @param userId 用户ID
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitCompetitionAnswers(CompetitionSubmitRequest submitRequest, Long userId) {
        if (submitRequest == null || userId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 获取竞赛ID
            Long competitionId = Long.valueOf(submitRequest.getCompetitionId());
            
            // 检查竞赛是否存在
            Competition competition = this.getById(competitionId);
            if (competition == null || competition.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 检查竞赛状态是否为进行中
            if (competition.getStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始或已结束，无法提交");
            }
            
            // 检查用户是否参与了该竞赛
            QueryWrapper<CompetitionParticipant> participantQueryWrapper = new QueryWrapper<>();
            participantQueryWrapper.eq("competition_id", competitionId)
                    .eq("user_id", userId);
            CompetitionParticipant participant = competitionParticipantMapper.selectOne(participantQueryWrapper);

            if (participant == null) {
                // 用户未参与竞赛，自动添加参与记录
                participant = new CompetitionParticipant();
                participant.setCompetitionId(competitionId);
                participant.setUserId(userId);
                participant.setJoinTime(LocalDateTime.now());
                participant.setScore(0); // 初始分数为0
                participant.setRank(0); // 初始排名为0
                participant.setCreateTime(LocalDateTime.now());
                participant.setUpdateTime(LocalDateTime.now());
                competitionParticipantMapper.insert(participant);
            }
            
            // 获取竞赛题目
            QueryWrapper<CompetitionQuestion> questionQueryWrapper = new QueryWrapper<>();
            questionQueryWrapper.eq("competition_id", competitionId);
            List<CompetitionQuestion> competitionQuestions = competitionQuestionMapper.selectList(questionQueryWrapper);
            
            // 检查提交的题目是否属于该竞赛
            Map<String, CompetitionSubmitRequest.QuestionSubmissionInfo> questionSubmissions = submitRequest.getQuestionSubmissions();
            
            // 处理每道题目的提交
            for (Map.Entry<String, CompetitionSubmitRequest.QuestionSubmissionInfo> entry : questionSubmissions.entrySet()) {
                String questionIdStr = entry.getKey();
                CompetitionSubmitRequest.QuestionSubmissionInfo submissionInfo = entry.getValue();
                
                // 检查题目ID是否一致
                if (!questionIdStr.equals(submissionInfo.getQuestionId())) {
                    log.warn("题目ID不一致: key={}, value={}", questionIdStr, submissionInfo.getQuestionId());
                    continue;
                }
                
                Long questionId = Long.valueOf(questionIdStr);
                
                // 检查题目是否属于该竞赛
                boolean isCompetitionQuestion = competitionQuestions.stream()
                        .anyMatch(cq -> cq.getQuestionId().equals(questionId));
                
                if (!isCompetitionQuestion) {
                    log.warn("题目{}不属于竞赛{}", questionId, competitionId);
                    continue;
                }
                

                // 创建题目提交记录
                SubmitRequest submitReq = new SubmitRequest();
                submitReq.setUserId(userId);
                submitReq.setQuestionId(questionId);
                submitReq.setLanguage(submissionInfo.getLanguage());
                submitReq.setCode(submissionInfo.getCode());
                submitReq.setCompetitionId(competitionId);
                
                // 调用题目提交服务
                try {
                    Long submitId = questionSubmitService.submit(submitReq);
                    log.info("竞赛{}用户{}提交题目{}成功，提交ID: {}", competitionId, userId, questionId, submitId);
                } catch (Exception e) {
                    log.error("竞赛{}用户{}提交题目{}失败: {}", competitionId, userId, questionId, e.getMessage());
                    // 继续处理下一个题目，不影响整体提交
                }
            }
            
            // 更新参与者的更新时间
            participant.setUpdateTime(LocalDateTime.now());
            competitionParticipantMapper.updateById(participant);
            
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交竞赛答案失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交竞赛答案失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取竞赛排行榜
     * @param competitionId 竞赛ID
     * @return 竞赛排行榜
     */
    @Override
    public List<CompetitionLeaderboardVO> getCompetitionLeaderboard(Long competitionId) {
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 查询竞赛是否存在
            Competition competition = this.getById(competitionId);
            if (competition == null || competition.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 构建查询条件
            QueryWrapper<CompetitionParticipant> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("competition_id", competitionId);
            queryWrapper.orderByDesc("score"); // 按得分降序排序
            queryWrapper.orderByAsc("join_time"); // 同分按参与时间升序排序
            
            // 查询排行榜
            List<CompetitionParticipant> participants = competitionParticipantMapper.selectList(queryWrapper);
            
            // 转换为VO对象
            List<CompetitionLeaderboardVO> leaderboardVOList = new ArrayList<>();
            for (int i = 0; i < participants.size(); i++) {
                CompetitionParticipant participant = participants.get(i);
                CompetitionLeaderboardVO vo = new CompetitionLeaderboardVO();
                vo.setRank(i + 1);
                vo.setUserId(participant.getUserId());
                vo.setScore(participant.getScore());
                vo.setJoinTime(participant.getJoinTime());
                vo.setIsSubmitted(participant.getIsSubmitted());
                
                // 查询用户信息
                User user = userMapper.selectById(participant.getUserId());
                if (user != null) {
                    vo.setUserName(user.getUserName());
                    vo.setUserAccount(user.getUserAccount());
                    vo.setUserAvatar(user.getUserAvatar());
                }
                
                // 查询用户在此竞赛的提交数和通过数
                QueryWrapper<QuestionSubmit> submitQueryWrapper = new QueryWrapper<>();
                submitQueryWrapper.eq("userId", participant.getUserId());
                submitQueryWrapper.eq("competitionId", competitionId);
                Long submitCount = questionSubmitMapper.selectCount(submitQueryWrapper);
                vo.setSubmitCount(submitCount != null ? submitCount.intValue() : 0);
                
                // 查询通过数
                submitQueryWrapper.clear();
                submitQueryWrapper.eq("userId", participant.getUserId());
                submitQueryWrapper.eq("competitionId", competitionId);
                submitQueryWrapper.eq("status", 2); // 2表示通过
                Long acceptCount = questionSubmitMapper.selectCount(submitQueryWrapper);
                vo.setAcceptCount(acceptCount != null ? acceptCount.intValue() : 0);
                
                // 计算通过的测试用例数量
                int passedTestCases = 0;
                submitQueryWrapper.clear();
                submitQueryWrapper.eq("userId", participant.getUserId())
                        .eq("competitionId", competitionId)
                        .eq("status", 2); // 通过的提交
                List<QuestionSubmit> submits = questionSubmitMapper.selectList(submitQueryWrapper);
                
                for (QuestionSubmit submit : submits) {
                    try {
                        String judgeInfoStr = submit.getJudgeInfo();
                        if (judgeInfoStr != null && !judgeInfoStr.isEmpty()) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            List<Map<String, Object>> judgeInfoList = objectMapper.readValue(
                                    judgeInfoStr, 
                                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                            );
                            
                            for (Map<String, Object> testCase : judgeInfoList) {
                                Object passedObj = testCase.get("passed");
                                if (passedObj != null && Integer.valueOf(passedObj.toString()) == 2) {
                                    passedTestCases++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析judgeInfo失败", e);
                    }
                }
                
                vo.setPassedTestCases(passedTestCases);
                
                // 重新计算得分并设置（每个通过的测试用例得10分）
                int calculatedScore = passedTestCases * 10;
                vo.setScore(calculatedScore);
                
                // 如果数据库中的得分与计算得分不一致，更新数据库
                if (participant.getScore() == null || participant.getScore() != calculatedScore) {
                    participant.setScore(calculatedScore);
                    competitionParticipantMapper.updateById(participant);
                    log.info("更新用户{}在竞赛{}中的得分: {}", participant.getUserId(), competitionId, calculatedScore);
                }
                
                leaderboardVOList.add(vo);
            }
            
            return leaderboardVOList;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取竞赛排行榜失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取竞赛排行榜失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Competition转换为CompetitionVO
     */
    private CompetitionVO competitionToVO(Competition competition) {
        CompetitionVO vo = CompetitionVO.fromCompetition(competition);
        
        // 设置创建人信息
        User creator = userMapper.selectById(competition.getCreatorId());
        if (creator != null) {
            vo.setCreatorAccount(creator.getUserAccount());
            vo.setCreatorName(creator.getUserName());
        }
        
        // 设置参与人数
        Integer participantCount = competitionMapper.getParticipantCount(competition.getId());
        vo.setParticipantCount(participantCount);
        
        // 设置题目数量
        Integer questionCount = competitionMapper.getQuestionCount(competition.getId());
        vo.setQuestionCount(questionCount);
        
        return vo;
    }

    /**
     * 交卷
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitPaper(Long competitionId, Long userId) {
        if (competitionId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 查询竞赛是否存在
            Competition competition = this.getById(competitionId);
            if (competition == null || competition.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 检查竞赛状态
            if (competition.getStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始或已结束，无法交卷");
            }
            
            // 检查用户是否参与了该竞赛
            QueryWrapper<CompetitionParticipant> participantQueryWrapper = new QueryWrapper<>();
            participantQueryWrapper.eq("competition_id", competitionId)
                    .eq("user_id", userId);
            CompetitionParticipant participant = competitionParticipantMapper.selectOne(participantQueryWrapper);
            
            if (participant == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "您未参与该竞赛，无法交卷");
            }
            
            // 检查用户是否已经交卷
            if (participant.getIsSubmitted() != null && participant.getIsSubmitted() == 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已交卷，不能重复交卷");
            }
            
            // 计算用户得分
            // 注意：由于数据库中可能还没有competitionId字段，我们先根据竞赛时间来查询用户在竞赛期间的提交
            // 获取竞赛开始和结束时间
            LocalDateTime startTime = competition.getStartTime();
            LocalDateTime endTime = competition.getEndTime();
            
            // 4. 获取该竞赛的所有题目
            QueryWrapper<CompetitionQuestion> questionQueryWrapper = new QueryWrapper<>();
            questionQueryWrapper.eq("competition_id", competitionId);
            List<CompetitionQuestion> competitionQuestions = competitionQuestionMapper.selectList(questionQueryWrapper);
            
            // 用于记录已通过的题目ID
            Set<Long> acceptedQuestionIds = new HashSet<>();
            // 用于记录通过的测试用例总数
            int totalPassedTestCases = 0;
            
            // 5. 遍历每道题目
            for (CompetitionQuestion competitionQuestion : competitionQuestions) {
                Long questionId = competitionQuestion.getQuestionId();
                
                // 获取该题目的信息
                Question question = questionMapper.selectById(questionId);
                if (question == null) {
                    log.warn("题目不存在，ID: {}", questionId);
                    continue;
                }
                
                // 查询用户对该题目在该竞赛中的提交
                QueryWrapper<QuestionSubmit> submitQueryWrapper = new QueryWrapper<>();
                submitQueryWrapper.eq("userId", userId)
                        .eq("questionId", questionId)
                        .eq("competitionId", competitionId) // 使用竞赛ID筛选
                        .orderByDesc("createTime");  // 按提交时间降序排序
                
                List<QuestionSubmit> submits = questionSubmitMapper.selectList(submitQueryWrapper);
                
                // 检查是否有通过的提交
                boolean hasAccepted = false;
                QuestionSubmit latestSubmit = null;
                
                if (!submits.isEmpty()) {
                    latestSubmit = submits.get(0); // 获取最新的提交
                    
                    for (QuestionSubmit submit : submits) {
                        if (submit.getStatus() != null && submit.getStatus() == 2) { // 状态为2表示通过
                            hasAccepted = true;
                            acceptedQuestionIds.add(questionId);
                            
                            // 解析judgeInfo计算通过的测试用例数量
                            try {
                                String judgeInfoStr = submit.getJudgeInfo();
                                if (judgeInfoStr != null && !judgeInfoStr.isEmpty()) {
                                    // 解析judgeInfo为JSON数组
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    List<Map<String, Object>> judgeInfoList = objectMapper.readValue(
                                            judgeInfoStr, 
                                            new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                                    );
                                    
                                    // 统计通过的测试用例数量
                                    int passedTestCases = 0;
                                    for (Map<String, Object> testCase : judgeInfoList) {
                                        // 检查passed字段是否为2(通过)
                                        Object passedObj = testCase.get("passed");
                                        if (passedObj != null && Integer.valueOf(passedObj.toString()) == 2) {
                                            passedTestCases++;
                                        }
                                    }
                                    
                                    // 累加到总通过测试用例数
                                    totalPassedTestCases += passedTestCases;
                                    log.info("题目{}通过了{}个测试用例", questionId, passedTestCases);
                                }
                            } catch (Exception e) {
                                log.error("解析judgeInfo失败", e);
                            }
                            
                            break;
                        }
                    }
                    
                    // 如果尚未判题或者未通过，则发送到RabbitMQ进行判题
                    if (!hasAccepted && latestSubmit != null) {
                        // 更新提交记录，添加竞赛ID
                        UpdateWrapper<QuestionSubmit> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("id", latestSubmit.getId());
                        QuestionSubmit updateSubmit = new QuestionSubmit();
                        updateSubmit.setCompetitionId(competitionId);
                        questionSubmitMapper.update(updateSubmit, updateWrapper);
                        
                        // 创建ExecuteCodeRequest并发送到RabbitMQ
                        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
                        executeCodeRequest.setCode(latestSubmit.getCode());
                        executeCodeRequest.setLanguage(latestSubmit.getLanguage());
                        executeCodeRequest.setQuestion(question);
                        executeCodeRequest.setUserId(userId);
                        executeCodeRequest.setQuestionSubmitId(latestSubmit.getId());
                        
                        try {
                            rabbitMQProducer.sendMessage("code_exchange", "routingkey", executeCodeRequest);
                            log.info("提交代码到RabbitMQ成功，竞赛ID: {}, 用户ID: {}, 题目ID: {}", 
                                     competitionId, userId, questionId);
                        } catch (Exception e) {
                            log.error("提交代码到RabbitMQ失败", e);
                            // 继续处理下一个题目，不影响整体提交
                        }
                    }
                }
            }
            
            // 6. 计算得分（每个通过的测试用例10分）
            int score = totalPassedTestCases * 10;
            log.info("用户{}在竞赛{}中通过了{}个测试用例，得分: {}", userId, competitionId, totalPassedTestCases, score);
            
            // 7. 更新用户得分和提交状态
            participant.setScore(score);
            participant.setIsSubmitted(1); // 标记为已提交
            participant.setUpdateTime(LocalDateTime.now());
            competitionParticipantMapper.updateById(participant);
            
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("交卷失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "交卷失败: " + e.getMessage());
        }
    }
} 