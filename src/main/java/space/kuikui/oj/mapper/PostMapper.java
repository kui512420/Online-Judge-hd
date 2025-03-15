package space.kuikui.oj.mapper;

import org.apache.ibatis.annotations.Mapper;
import space.kuikui.oj.model.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 30767
* @description 针对表【post(帖子)】的数据库操作Mapper
* @createDate 2025-03-15 16:12:52
* @Entity space.kuikui.oj.model.entity.Post
*/
@Mapper
public interface PostMapper extends BaseMapper<Post> {

}




