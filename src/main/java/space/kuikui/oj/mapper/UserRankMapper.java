package space.kuikui.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import space.kuikui.oj.model.entity.UserRank;

@Mapper
public interface UserRankMapper extends BaseMapper<UserRank> {
}
