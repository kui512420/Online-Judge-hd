package space.kuikui.oj.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author kuikui
 * @date 2025/3/27 0:10
 */
@Data
public class UserFileRequest {
    private MultipartFile file;
}
