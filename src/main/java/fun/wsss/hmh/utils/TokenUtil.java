package fun.wsss.hmh.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Token 工具类
 * @author wsss
 */
public class TokenUtil {

    private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY") != null ?
            System.getenv("JWT_SECRET_KEY") : "default-dev-key-change-in-production";

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
            try {
                // 验证token有效性
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                        .build()
                        .verify(token);

                Integer userId = jwt.getClaim("userId").asInt();
                if (userId == null) {
                    return null;
                }
                return userService.findById(userId);
            } catch (TokenExpiredException e) {
                // token已过期
                return null;
            } catch (JWTVerificationException e) {
                // token验证失败
                return null;
            }
        }
        return null;
    }

    /**
     * 从请求头中获取用户ID（不验证token，仅用于非关键操作）
     * @param request HTTP请求
     * @return 用户ID，如果token无效则返回null
     */
    public static Integer getUserIdFromTokenUnsafe(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                DecodedJWT jwt = JWT.decode(token);
                return jwt.getClaim("userId").asInt();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
} 