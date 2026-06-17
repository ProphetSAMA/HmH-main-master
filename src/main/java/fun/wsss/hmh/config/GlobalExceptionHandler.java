package fun.wsss.hmh.config;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "internal_error");
        response.put("message", e.getMessage());

        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "unauthorized");
        response.put("message", e.getMessage());

        return ResponseEntity.status(401).body(response);
    }
} 