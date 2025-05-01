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
import space.kuikui.oj.model.vo.CompetitionVO;
import space.kuikui.oj.service.CompetitionService;

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
    public BaseResponse<Long> addCompetition(@RequestHeader(value = "AccessToken", required = false) String accessToken,
                                                @RequestBody CompetitionAddRequest addRequest) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARMS_ERROR, "参数错误");
        }
        
        // 从令牌中获取用户ID
        Long creatorId = null;
        try {
            log.info("添加竞赛请求: {}", addRequest);
            
            if (accessToken == null || accessToken.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
            }
            
            JwtLoginUtils jwtLoginUtils = new JwtLoginUtils();
            // 注意：由于原有代码使用的是Long类型，但Competition使用的是Integer
            // 这里进行类型转换
            Long userId = (Long) jwtLoginUtils.jwtPeAccess(accessToken).get("id");
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
            
            JwtLoginUtils jwtLoginUtils = new JwtLoginUtils();
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
} 