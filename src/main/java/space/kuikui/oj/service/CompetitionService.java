package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import space.kuikui.oj.model.dto.CompetitionAddRequest;
import space.kuikui.oj.model.dto.CompetitionRequest;
import space.kuikui.oj.model.dto.CompetitionSubmitRequest;
import space.kuikui.oj.model.entity.Competition;
import space.kuikui.oj.model.entity.CompetitionParticipant;
import space.kuikui.oj.model.vo.CompetitionVO;
import space.kuikui.oj.model.vo.CompetitionLeaderboardVO;
import java.util.List;

/**
 * 竞赛服务接口
 * @author kuikui
 * @date 2025/4/28 16:00
 */
public interface CompetitionService extends IService<Competition> {
    
    /**
     * 分页查询竞赛列表
     * @param competitionRequest 查询条件
     * @return 分页结果
     */
    Page<CompetitionVO> pageCompetitions(CompetitionRequest competitionRequest);
    
    /**
     * 添加竞赛
     * @param addRequest 添加请求
     * @param creatorId 创建人ID
     * @return 竞赛ID
     */
    Long addCompetition(CompetitionAddRequest addRequest, Long creatorId);
    
    /**
     * 更新竞赛
     * @param updateRequest 更新请求
     * @param userId 更新人ID
     * @return 是否更新成功
     */
    boolean updateCompetition(CompetitionAddRequest updateRequest, Long userId);
    
    /**
     * 获取竞赛详情
     * @param id 竞赛ID
     * @return 竞赛详情
     */
    CompetitionVO getCompetitionDetail(Long id);
    
    /**
     * 更新竞赛状态
     * 根据当前时间自动更新竞赛状态
     */
    void updateCompetitionStatus();
    
    /**
     * 用户参与竞赛
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean joinCompetition(Long competitionId, Long userId);
    
    /**
     * 提交竞赛答案
     * @param submitRequest 提交请求
     * @param userId 用户ID
     * @return 是否提交成功
     */
    boolean submitCompetitionAnswers(CompetitionSubmitRequest submitRequest, Long userId);
    
    /**
     * 获取竞赛排行榜
     * @param competitionId 竞赛ID
     * @return 竞赛排行榜
     */
    List<CompetitionLeaderboardVO> getCompetitionLeaderboard(Long competitionId);
    
    /**
     * 交卷
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean submitPaper(Long competitionId, Long userId);
} 