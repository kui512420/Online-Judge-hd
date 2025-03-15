package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import space.kuikui.oj.model.entity.Post;
import space.kuikui.oj.service.PostService;
import space.kuikui.oj.mapper.PostMapper;
import org.springframework.stereotype.Service;

/**
* @author 30767
* @description 针对表【post(帖子)】的数据库操作Service实现
* @createDate 2025-03-15 16:12:52
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

}




