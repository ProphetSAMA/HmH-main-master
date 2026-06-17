package fun.wsss.hmh.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author Wsssfun
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        // 记录详细异常信息到日志
        log.error("系统异常: ", e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "internal_error");
        // 不返回具体异常信息给客户端，防止信息泄露
        response.put("message", "系统内部错误，请联系管理员");

        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException e) {
        log.warn("安全异常: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "unauthorized");
        response.put("message", "权限验证失败");

        return ResponseEntity.status(401).body(response);
    }
} 