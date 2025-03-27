package space.kuikui.oj;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import space.kuikui.oj.common.JwtLoginUtils;
import space.kuikui.oj.model.entity.User;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest
class OjApplicationTests {

    @Resource
    private JwtLoginUtils jwtLoginUtils;


    @Test
    void jwtTest() {
        User user = new User();
        user.setId(423423432l);
        user.setUserAccount("sdasdasdas");
        //设token
        String token = jwtLoginUtils.jwtBdAccess(user);
        //校验token
        Map<Object, Object> map = jwtLoginUtils.jwtPeAccess(token);
        System.out.println(map.get("id"));
    }

}
