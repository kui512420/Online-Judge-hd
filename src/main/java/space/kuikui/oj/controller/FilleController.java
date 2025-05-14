package space.kuikui.oj.controller;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.service.UserService;

import java.util.Collections;import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
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

    private final static String USER_HEADER_PATH = "C:\\Users\\30767\\Desktop\\git\\userHeader\\";

    private final static String QUESTION_CONTENT_PATH = "C:\\Users\\30767\\Desktop\\git\\questionContent\\";
    /**
     * @todo 上传头像
     * @param file
     * @param accesstoken
     * @throws IOException
     */
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "上传文件", description = "支持文件上传，返回 JSON 格式响应",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                           )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "文件上传成功",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })

    public void uploadFile(@RequestParam("file") MultipartFile file,@RequestHeader(value = "Accesstoken",required = false) String accesstoken) throws IOException {
        // 检查用户头像目录是否存在
        boolean isExist = FileUtil.exist(USER_HEADER_PATH);

        // 不存在就创建目录
        if(!isExist){
            FileUtil.mkdir(USER_HEADER_PATH);
        }
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(accesstoken);
        String id = map.get("id").toString();
        StringBuilder header = new StringBuilder();
        header.append(USER_HEADER_PATH).append(id).append(".png");
        // 获取文件的类型
        String type = FileTypeUtil.getType(file.getInputStream());
        if(!StringUtils.containsAny(type,"jpg","jpeg","png")){
            System.out.println("非图片文件");
        }else{
            FileWriter writer = new FileWriter(String.valueOf(header));
            writer.writeFromStream(file.getInputStream());
            userService.updateUserAvatar(Long.valueOf(id),"api/file/userheader/"+id+".png");
        }
    }

    /**
     * @todo 上传题目图片
     * @param file
     * @param accesstoken
     * @throws IOException
     */
    @PostMapping(value = "/file/upload/question", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "上传文件", description = "支持文件上传，返回 JSON 格式响应",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "文件上传成功",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })

    public String uploadQuestionFile(@RequestParam("file") MultipartFile file,@RequestHeader(value = "Accesstoken",required = false) String accesstoken) throws IOException {
        // 检查用户头像目录是否存在
        boolean isExist = FileUtil.exist(QUESTION_CONTENT_PATH);

        // 不存在就创建目录
        if(!isExist){
            FileUtil.mkdir(QUESTION_CONTENT_PATH);
        }
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(accesstoken);
        String id = new Date().getTime()+"";
        StringBuilder header = new StringBuilder();
        header.append(QUESTION_CONTENT_PATH).append(id).append(".png");
        // 获取文件的类型
        String type = FileTypeUtil.getType(file.getInputStream());
        if(!StringUtils.containsAny(type,"jpg","jpeg","png")){
            System.out.println("非图片文件");
        }else{
            FileWriter writer = new FileWriter(String.valueOf(header));
            writer.writeFromStream(file.getInputStream());
        }
        return id;
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
    /**
     * @todo 显示题目中的图片
     * @param filename
     * @return
     * @throws IOException
     */
    @GetMapping("/questionContent/{filename:.+}")
    public ResponseEntity<byte[]> getQuestionContent(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(QUESTION_CONTENT_PATH).resolve(filename).normalize();
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
