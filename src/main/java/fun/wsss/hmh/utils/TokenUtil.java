package fun.wsss.hmh.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Token 工具类
 * @author wsss
 */
public class TokenUtil {
    
    private TokenUtil() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * 从请求头中获取用户信息
     * @param request HTTP请求
     * @param userService 用户服务
     * @return 用户信息，如果token无效则返回null
     */
    public static User getUserFromToken(HttpServletRequest request, UserService userService) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            DecodedJWT jwt = JWT.decode(token);
            Integer userId = jwt.getClaim("userId").asInt();
            return userService.findById(userId);
        }
        return null;
    }
} 