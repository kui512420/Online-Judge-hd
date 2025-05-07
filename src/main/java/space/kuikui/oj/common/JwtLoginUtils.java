package space.kuikui.oj.common;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.kuikui.oj.exception.BusinessException;
import space.kuikui.oj.model.entity.User;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtLoginUtils {
    @Value("${jwt.sign}")
    private  String sign;
    @Value("${jwt.timeAccess}")
    private  long timeAccess;
    //加密-校验token
    public  String jwtBdAccess(User u) {
        JwtBuilder jwtBuilder = Jwts.builder();
        ZonedDateTime createTimeZoned = u.getCreateTime().toInstant().atZone(ZoneId.systemDefault());
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String createTimeStr = createTimeZoned.format(formatter);
        String token = jwtBuilder
                .setHeaderParam("alg","HS256")
                .setHeaderParam("type","JWT")
                .claim("userAccount",u.getUserAccount())
                .claim("userName",u.getUserName())
                .claim("id",u.getId()+"")
                .claim("userRole",u.getUserRole())
                .claim("createTime",createTimeStr)
                .claim("userAvatar",u.getUserAvatar())
                .claim("userProfile",u.getUserProfile())
                .claim("email",u.getEmail())
                .setExpiration(new Date(System.currentTimeMillis()+timeAccess))
                .signWith(SignatureAlgorithm.HS256,sign)
                .compact();
        return token;
    }
    //解密-校验token
    public Map<Object,Object> jwtPeAccess(String token) {
        JwtParser jwtParser = Jwts.parser();
        Jws<Claims> claimsJwts = null;
        try{
            claimsJwts = jwtParser.setSigningKey(sign).parseClaimsJws(token);
        }catch (Exception e){
            throw  new BusinessException(40000,"toke校验失败");
        }
        Claims claims = claimsJwts.getBody();
        Map<Object,Object> map = new LinkedHashMap();
        map.put("id", claims.get("id")+"");
        map.put("userAccount", claims.get("userAccount"));
        map.put("userRole", claims.get("userRole"));
        map.put("createTime", claims.get("createTime"));
        map.put("userName", claims.get("userName"));
        map.put("email", claims.get("email"));
        map.put("userAvatar",claims.get("userAvatar"));
        map.put("userProfile", claims.get("userProfile"));
        return map;
    }

}
