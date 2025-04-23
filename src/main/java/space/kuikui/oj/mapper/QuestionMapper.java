package space.kuikui.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import space.kuikui.oj.model.dto.QuestionPostRequest;
import space.kuikui.oj.model.entity.Question;
import java.util.List;

/**
* @author 30767
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2025-03-16 20:33:35
* @Entity generator.domain.Question
*/
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    @Select("select * from question order by updateTime desc")
    List<Question> selectAllQuestion();
    @Update("UPDATE question " +
            "SET title = #{title}, " +
            "    content = #{content}, " +
            "    tags = #{tags}, " +
            "    judgeCase = #{judgeCase}, " +
            "    judgeConfig = #{judgeConfig}, " +
            "    updateTime = NOW() " +
            "WHERE id = #{id}")
    int updateQuestion(QuestionPostRequest questionPostRequest);
}




