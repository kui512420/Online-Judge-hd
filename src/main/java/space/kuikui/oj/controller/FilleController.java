package space.kuikui.oj.controller;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.service.UserService;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author kuikui
 * @date 2025/3/27 0:08
 */
@RestController
@RequestMapping("/api/file")
public class FilleController {

    @Resource
    private JwtLoginUtils jwtLoginUtils;
    @Resource
    private UserService userService;

    private final static String USER_HEADER_PATH = "C:\\Users\\30767\\Desktop\\git\\qq\\userHeader\\";

    /**
     * @todo 上传头像
     * @param file
     * @param accesstoken
     * @throws IOException
     */
    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file,@RequestHeader(value = "Accesstoken",required = false) String accesstoken) throws IOException {
        // 检查用户头像目录是否存在
        boolean isExist = FileUtil.exist(USER_HEADER_PATH);
        // 不存在就创建目录
        if(!isExist){
            FileUtil.mkdir(USER_HEADER_PATH);
        }
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(accesstoken);
        long id = (long) map.get("id");
        StringBuilder header = new StringBuilder();
        header.append(USER_HEADER_PATH).append(id).append(".png");
        // 获取文件的类型
        String type = FileTypeUtil.getType(file.getInputStream());
        if(!StringUtils.containsAny(type,"jpg","jpeg","png")){
            System.out.println("非图片文件");
        }else{
            FileWriter writer = new FileWriter(String.valueOf(header));
            writer.writeFromStream(file.getInputStream());
            userService.updateUserAvatar(id,"api/file/userheader/"+id+".png");
        }
    }

    /**
     * @todo 显示头像
     * @param filename
     * @return
     * @throws IOException
     */
    @GetMapping("/userheader/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(USER_HEADER_PATH).resolve(filename).normalize();
        // 读取文件内容
        byte[] imageBytes = Files.readAllBytes(filePath);
        // 设置Content-Type为image/jpeg
        MediaType mediaType = MediaType.IMAGE_JPEG;
        // 构建响应
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageBytes);
    }

}
