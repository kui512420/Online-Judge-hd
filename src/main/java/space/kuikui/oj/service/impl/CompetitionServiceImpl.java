package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import space.kuikui.oj.model.dto.CompetitionAddRequest;
import space.kuikui.oj.model.dto.CompetitionRequest;
import space.kuikui.oj.model.entity.Competition;
import space.kuikui.oj.model.entity.CompetitionQuestion;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.User;
import space.kuikui.oj.model.vo.CompetitionVO;
import space.kuikui.oj.service.CompetitionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
            if (now.isAfter(endTime)) {
                competition.setStatus(2); // 已结束
            } else if (now.isAfter(startTime)) {
                competition.setStatus(1); // 进行中
            } else {
                competition.setStatus(0); // 未开始
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
            log.error("更新竞赛状态失败", e);
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
} 