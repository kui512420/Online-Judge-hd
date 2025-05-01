package space.kuikui.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import space.kuikui.oj.common.ErrorCode;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.mapper.TagMapper;
import space.kuikui.oj.model.dto.TagRequest;
import space.kuikui.oj.model.entity.Question;
import space.kuikui.oj.model.entity.Tag;
import space.kuikui.oj.service.TagService;

/**
 * @author kuikui
 * @date 2025/4/25 16:50
 */
@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagMapper tagMapper;

    @Override
    public void addTag(Tag tag) {
        try {
            tagMapper.insert(tag);
        }catch (Exception e) {
            throw  new BusinessException(ErrorCode.PARMS_ERROR,"标签已经存在！");
        }
    }

    @Override
    public Page<Tag> list(TagRequest tagRequest) {
        Integer page = tagRequest.getPage();
        Integer count = tagRequest.getCount();
        Integer type = tagRequest.getType();

        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        // 创建分页对象
        Page<Tag> pageOj = new Page<>(page, count);
        switch (type){
            case 0:
                break;
                case 1:
                    queryWrapper.eq("name",tagRequest.getTag());
                    break;
            default:
                throw new BusinessException(ErrorCode.PARMS_ERROR,"参数异常");
        }
        return tagMapper.selectPage(pageOj,queryWrapper);
    }

    @Override
    public int del(Long id) {
        return tagMapper.deleteById(id);
    }
}
