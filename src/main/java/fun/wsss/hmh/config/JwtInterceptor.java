package fun.wsss.hmh.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT拦截器
 *
 * @author Wsssfun
 */
public class JwtInterceptor implements HandlerInterceptor {

    private static final String SECRET_KEY = "your-secret-key";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> excludePaths = Arrays.asList(
            "/api/user/login",
            "/api/user/logout"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        System.out.println("Request URI: " + requestURI); // 添加调试日志

        // 检查是否是豁免路径
        if (excludePaths.stream().anyMatch(requestURI::startsWith)) {
            return true;
        }

        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        System.out.println("Authorization header: " + token); // 添加调试日志

        if (token == null || !token.startsWith("Bearer ")) {
            handleError(response, "missing_token", "用户未登录");
            return false;
        }

        try {
            // 验证token
            token = token.substring(7);
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build();
            verifier.verify(token);
            return true;
        } catch (TokenExpiredException e) {
            handleError(response, "token_expired", "登录已过期，请重新登录");
            return false;
        } catch (JWTVerificationException e) {
            handleError(response, "invalid_token", "无效的登录信息");
            return false;
        }
    }

    private void handleError(HttpServletResponse response, String error, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("message", message);

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        System.out.println("Error response: " + jsonResponse); // 添加调试日志
        response.getWriter().write(jsonResponse);
    }
} 