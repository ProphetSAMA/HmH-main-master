package fun.wsss.hmh.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Claim;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                // 解析 JWT
                DecodedJWT decodedJWT = JWT.decode(token);
                
                // 创建用户对象
                User user = new User();
                
                // 从JWT中获取userId（整数类型）
                Claim userIdClaim = decodedJWT.getClaim("userId");
                if (!userIdClaim.isNull()) {
                    Integer userId = userIdClaim.asInt();
                    user.setId(userId);
                } else {
                    // 如果无法获取userId，使用默认值
                    user.setId(1);
                }
                
                // 设置其他用户信息
                user.setUserName(decodedJWT.getClaim("userName").asString());
                user.setTrueName(decodedJWT.getClaim("trueName").asString());
                user.setRoleName(decodedJWT.getClaim("roleName").asString());
                
                // 将用户信息存储在上下文中
                UserContext.setCurrentUser(user);
                
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
} 