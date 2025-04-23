package space.kuikui.oj.utils;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;


import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author kuikui
 * @date 2025/3/16 23:36
 */
@Component
public class CaptchaUtil {

    private final DefaultKaptcha kaptchaProducer;
    private final static String CAPTCHA_SESSION_KEY = "cache_verify_code_text";

    public CaptchaUtil() {
        this.kaptchaProducer = createKaptchaProducer();
    }

    /**
     * 实例化验证码
     */
    private DefaultKaptcha createKaptchaProducer() {
        // 实例配置参数对象
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.textproducer.char.space", "4");
        properties.setProperty("kaptcha.image.width", "120");
        properties.setProperty("kaptcha.image.height", "50");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier,monospace,sans-serif");

        // 依据配置创建实例
        Config config = new Config(properties);
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(config);
        return kaptcha;
    }

    /**
     * 生成验证码图片
     */
    public void outCodeImg(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true); // 获取或创建Session
        String captchaText = kaptchaProducer.createText();

        // 存储验证码文本到Session
        session.setAttribute(CAPTCHA_SESSION_KEY, captchaText);

        BufferedImage image = kaptchaProducer.createImage(captchaText);
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setDateHeader("Expires", 0);

        // 将验证码图片写入响应输出流
        javax.imageio.ImageIO.write(image, "png", response.getOutputStream());
    }

    /**
     * 验证验证码
     */
    public boolean validateCode(HttpServletRequest request, String input) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String storedCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);

        if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(input)) {
            return false;
        }else{
            session.removeAttribute(CAPTCHA_SESSION_KEY);
        }

        return true;
    }

}
