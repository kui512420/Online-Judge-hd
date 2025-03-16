package space.kuikui.oj.common;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import space.kuikui.oj.model.entity.User;


import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtLoginUtils {
    private static String sign = "j5124200";
    private static long timeAccess = 1000 * 60 * 30 ;
    private static long timeRefresh = 1000 * 60 * 60 * 24 * 7 ;
    //加密-校验token
    public  String jwtBdAccess(User u) {
        JwtBuilder jwtBuilder = Jwts.builder();
        String token = jwtBuilder
                .setHeaderParam("alg","HS256")
                .setHeaderParam("type","JWT")
                .claim("userAccount",u.getUserAccount())
                .claim("username",u.getUserName())
                .claim("id",u.getId())
                .claim("userRole",u.getUserRole())
                .claim("userAvatar",u.getUserAvatar())
                .setExpiration(new Date(System.currentTimeMillis()+timeAccess))
                .signWith(SignatureAlgorithm.HS256,sign)
                .compact();
        return token;
    }
    //解密-校验token
    public Map<Object,Object> jwtPeAccess(String token) {
        JwtParser jwtParser = Jwts.parser();
        Jws<Claims> claimsJwts = jwtParser.setSigningKey(sign).parseClaimsJws(token);
        Claims claims = claimsJwts.getBody();
        Map<Object,Object> map = new LinkedHashMap();
        map.put("id", claims.get("id"));
        map.put("username", claims.get("username"));
        map.put("nickname", claims.get("nickname"));
        map.put("type", claims.get("type"));
        map.put("headsrc", claims.get("headsrc"));
        return map;
    }

    //加密-校验token
    public  String jwtBdRefresh(Long id, HttpServletRequest request) {
        JwtBuilder jwtBuilder = Jwts.builder();
        String Ua = request.getHeader("User-Agent");
        String token = jwtBuilder
                .setHeaderParam("alg","HS256")
                .setHeaderParam("type","JWT")
                .claim("id",id)
                .claim("Ua",Ua)
                .setExpiration(new Date(System.currentTimeMillis()+timeRefresh))
                .signWith(SignatureAlgorithm.HS256,sign)
                .compact();
        return token;
    }
    //解密-刷新token
    public Map<Object,Object> jwtPeRefresh(String refreshToken) {
        JwtParser jwtParser = Jwts.parser();
        Jws<Claims> claimsJwts = jwtParser.setSigningKey(sign).parseClaimsJws(refreshToken);
        Claims claims = claimsJwts.getBody();
        Map<Object,Object> map = new LinkedHashMap();
        map.put("id", claims.get("id"));
        map.put("Ua", claims.get("Ua"));
        return map;
    }

}
