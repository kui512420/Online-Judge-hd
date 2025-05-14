package space.kuikui.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import space.kuikui.oj.common.BaseResponse;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.common.ResultUtils;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.dto.CompetitionAddRequest;
import space.kuikui.oj.model.dto.CompetitionRequest;
import space.kuikui.oj.model.dto.CompetitionSubmitRequest;
import space.kuikui.oj.model.entity.CompetitionQuestion;
import space.kuikui.oj.model.entity.CompetitionParticipant;
import space.kuikui.oj.model.vo.CompetitionVO;
import space.kuikui.oj.model.vo.QuestionListVo;
import space.kuikui.oj.model.vo.CompetitionLeaderboardVO;
import space.kuikui.oj.service.CompetitionQuestionService;
import space.kuikui.oj.service.CompetitionService;

import java.util.List;

/**
 * 竞赛管理控制器
 * @author kuikui
 * @date 2025/4/28 16:40
 */
@RestController
@RequestMapping("/api/competition")
@Slf4j
public class CompetitionController {
    
    @Resource
    private CompetitionService competitionService;
    @Resource
    private CompetitionQuestionService competitionQuestionService;
    @Resource
    private JwtLoginUtils jwtLoginUtils;
    
    /**
     * 分页查询竞赛列表
     * @param competitionRequest 请求参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public BaseResponse<Page<CompetitionVO>> listCompetitions(@RequestBody CompetitionRequest competitionRequest) {
        if (competitionRequest == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        try {
            Page<CompetitionVO> page = competitionService.pageCompetitions(competitionRequest);
            return ResultUtils.success("获取竞赛列表成功", page);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取竞赛列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取竞赛列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加竞赛
     * @param accessToken 访问令牌
     * @param addRequest 添加请求
     * @return 竞赛ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addCompetition(
                                                @RequestBody CompetitionAddRequest addRequest,@RequestHeader(value = "Accesstoken", required = false) String accessToken) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        // 从令牌中获取用户ID
        Long creatorId = null;
        try {
            log.info("添加竞赛请求: {}", addRequest);

            // 注意：由于原有代码使用的是Long类型，但Competition使用的是Integer
            // 这里进行类型转换
            Long userId = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(accessToken).get("id")) ;
            if (userId == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录信息获取失败");
            }
            creatorId = userId;
            
            Long competitionId = competitionService.addCompetition(addRequest, creatorId);
            return ResultUtils.success("添加竞赛成功", competitionId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加竞赛失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加竞赛失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取竞赛详情
     * @param id 竞赛ID
     * @return 竞赛详情
     */
    @GetMapping("/detail/{id}")
    public BaseResponse<CompetitionVO> getCompetitionDetail(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        try {
            CompetitionVO competition = competitionService.getCompetitionDetail(id);
            return ResultUtils.success("获取竞赛详情成功", competition);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取竞赛详情失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取竞赛详情失败: " + e.getMessage());
        }
    }
    /**
     * 获取竞赛题目
     * @param id 竞赛ID
     * @return 竞赛题目
     */
    @GetMapping("/questions/{id}")
    public BaseResponse<List<QuestionListVo>> getCompetitionQuestions(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        try {
            CompetitionVO competition = competitionService.getCompetitionDetail(id);

            // 判断竞赛是否开始
            if (competition.getStatus() == 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始，无法查看题目");
            }
            List<QuestionListVo> competitionQuestions = competitionQuestionService.getQuestions(id);
            return ResultUtils.success("获取竞赛题目成功", competitionQuestions);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取竞赛详情失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取竞赛详情失败: " + e.getMessage());
        }
    }
    /**
     * 删除竞赛（逻辑删除）
     * @param accessToken 访问令牌
     * @param id 竞赛ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteCompetition(@RequestHeader(value = "AccessToken", required = false) String accessToken,
                                                 @PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        // 从令牌中获取用户角色，验证权限
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
            }
            
            String userRole = (String) jwtLoginUtils.jwtPeAccess(accessToken).get("userRole");
            
            // 仅管理员可删除
            if (!"admin".equals(userRole)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限操作");
            }
            
            // 获取竞赛信息
            CompetitionVO competition = competitionService.getCompetitionDetail(id);
            if (competition == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 逻辑删除
            boolean result = competitionService.removeById(id);
            if (result) {
                return ResultUtils.success("删除竞赛成功", true);
            } else {
                return ResultUtils.success("删除竞赛失败", false);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除竞赛失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除竞赛失败: " + e.getMessage());
        }
    }
    
    /**
     * 参与竞赛
     * @param accessToken 访问令牌
     * @param competitionId 竞赛ID
     * @return 操作结果
     */
    @PostMapping("/join/{competitionId}")
    public BaseResponse<Boolean> joinCompetition(@RequestHeader(value = "Accesstoken", required = false) String accessToken,
                                                @PathVariable Long competitionId) {
        if (competitionId == null) {
            return ResultUtils.error(ErrorCode.PARMS_ERROR.getCode(),ErrorCode.PARMS_ERROR.getMessage(),false);
        }
        
        try {
            // 从令牌中获取用户ID
            Long userId = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(accessToken).get("id"));

            // 获取竞赛信息
            CompetitionVO competition = competitionService.getCompetitionDetail(competitionId);
            if (competition == null) {
                return ResultUtils.error(50000,"竞赛不存在",false);
            }
            // 判断竞赛状态是否为进行中
            if (competition.getStatus() != 1) {
                return ResultUtils.error(50000,"竞赛未开始或已结束，无法参与",false);
            }
            
            // 调用Service方法处理参与逻辑
            boolean result = competitionService.joinCompetition(competitionId, userId);
            return ResultUtils.success("参与竞赛成功", result);
        } catch (BusinessException e) {
            return ResultUtils.error(50000,"错误",false);
        }
    }
    
    /**
     * 提交竞赛答案
     * @param accessToken 访问令牌
     * @param submitRequest 提交请求
     * @return 操作结果
     */
    @PostMapping("/submit")
    public BaseResponse<Boolean> submitCompetitionAnswers(
            @RequestHeader(value = "Accesstoken", required = false) String accessToken,
            @RequestBody CompetitionSubmitRequest submitRequest) {
        
        // 参数校验
        if (submitRequest == null || submitRequest.getCompetitionId() == null 
                || submitRequest.getQuestionSubmissions() == null || submitRequest.getQuestionSubmissions().isEmpty()) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 从令牌中获取用户ID
            if (accessToken == null || accessToken.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
            }
            
            Long userId = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(accessToken).get("id"));
            if (userId == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录信息获取失败");
            }
            
            // 获取竞赛信息
            Long competitionId = Long.valueOf(submitRequest.getCompetitionId());
            CompetitionVO competition = competitionService.getCompetitionDetail(competitionId);
            if (competition == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 判断竞赛状态是否为进行中
            if (competition.getStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始或已结束，无法提交");
            }
            
            // 调用Service方法处理提交逻辑
            boolean result = competitionService.submitCompetitionAnswers(submitRequest, userId);
            
            if (result) {
                return ResultUtils.success("提交竞赛答案成功", true);
            } else {
                return ResultUtils.error(ErrorCode.OPERATION_ERROR.getCode(), "提交竞赛答案失败", false);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交竞赛答案失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交竞赛答案失败: " + e.getMessage());
        }
    }

    /**
     * 编辑竞赛
     * @param accessToken 访问令牌
     * @param updateRequest 更新请求
     * @return 操作结果
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editCompetition(
            @RequestHeader(value = "Accesstoken", required = false) String accessToken,
            @RequestBody CompetitionAddRequest updateRequest) {
        
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 从令牌中获取用户角色，验证权限
            if (accessToken == null || accessToken.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
            }
            
            String userRole = (String) jwtLoginUtils.jwtPeAccess(accessToken).get("userRole");
            
            // 仅管理员可编辑
            if (!"admin".equals(userRole)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限操作");
            }
            
            // 获取竞赛信息，确认存在
            CompetitionVO competition = competitionService.getCompetitionDetail(updateRequest.getId());
            if (competition == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 更新竞赛信息
            Long userId = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(accessToken).get("id"));
            boolean success = competitionService.updateCompetition(updateRequest, userId);
            
            if (success) {
                return ResultUtils.success("编辑竞赛成功", true);
            } else {
                return ResultUtils.error(ErrorCode.OPERATION_ERROR.getCode(), "编辑竞赛失败", false);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑竞赛失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "编辑竞赛失败: " + e.getMessage());
        }
    }

    /**
     * 定时任务：每分钟更新竞赛状态
     * 根据当前时间自动更新竞赛状态（未开始->进行中->已结束）
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void updateCompetitionStatus() {
        try {
            competitionService.updateCompetitionStatus();
        } catch (Exception e) {
            log.error("定时更新竞赛状态失败", e);
        }
    }

    /**
     * 获取竞赛排行榜
     * @param competitionId 竞赛ID
     * @return 竞赛排行榜
     */
    @GetMapping("/leaderboard/{competitionId}")
    public BaseResponse<List<CompetitionLeaderboardVO>> getCompetitionLeaderboard(@PathVariable Long competitionId) {
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        try {
            // 获取竞赛信息
            CompetitionVO competition = competitionService.getCompetitionDetail(competitionId);
            if (competition == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 获取竞赛排行榜
            List<CompetitionLeaderboardVO> leaderboard = competitionService.getCompetitionLeaderboard(competitionId);
            return ResultUtils.success("获取竞赛排行榜成功", leaderboard);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取竞赛排行榜失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取竞赛排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 交卷
     * @param accessToken 访问令牌
     * @param competitionId 竞赛ID
     * @param submitRequest 提交请求，包含用户代码
     * @return 操作结果
     */
    @PostMapping("/submit-paper/{competitionId}")
    public BaseResponse<Boolean> submitPaper(
            @RequestHeader(value = "Accesstoken", required = false) String accessToken,
            @PathVariable Long competitionId,
            @RequestBody(required = false) CompetitionSubmitRequest submitRequest) {
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        try {
            // 从令牌中获取用户ID
            Long userId = Long.valueOf((String) jwtLoginUtils.jwtPeAccess(accessToken).get("id"));
            if (userId == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录信息获取失败");
            }
            
            // 获取竞赛信息
            CompetitionVO competition = competitionService.getCompetitionDetail(competitionId);
            if (competition == null) {
                throw new BusinessException(ErrorCode.PARMS_ERROR, "竞赛不存在");
            }
            
            // 判断竞赛状态是否为进行中
            if (competition.getStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始或已结束，无法交卷");
            }
            
            // 调用Service方法处理交卷逻辑
            boolean result;
            if (submitRequest != null && submitRequest.getQuestionSubmissions() != null 
                    && !submitRequest.getQuestionSubmissions().isEmpty()) {
                // 有提交代码数据，先提交代码再交卷
                submitRequest.setCompetitionId(competitionId.toString());
                competitionService.submitCompetitionAnswers(submitRequest, userId);
                result = competitionService.submitPaper(competitionId, userId);
            } else {
                // 无提交代码数据，直接交卷
                result = competitionService.submitPaper(competitionId, userId);
            }
            
            return ResultUtils.success("交卷成功", result);
        } catch (BusinessException e) {
            return ResultUtils.error(e.getCode(), e.getMessage(), false);
        } catch (Exception e) {
            log.error("交卷失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "交卷失败: " + e.getMessage(), false);
        }
    }
} 