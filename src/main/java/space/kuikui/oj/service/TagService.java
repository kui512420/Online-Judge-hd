package space.kuikui.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import space.kuikui.oj.model.dto.TagRequest;
import space.kuikui.oj.model.entity.Tag;

/**
 * @author kuikui
 * @date 2025/4/25 16:50
 */

public interface TagService {
    void addTag(Tag tag);
    Page<Tag> list(TagRequest tagRequest);

    int del(Long id);
}
