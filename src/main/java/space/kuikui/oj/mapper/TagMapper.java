package space.kuikui.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import space.kuikui.oj.model.entity.Tag;

/**
 * @author kuikui
 * @date 2025/4/25 16:51
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
