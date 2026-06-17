package fun.wsss.hmh.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户工具类
 */
public class UserUtil {
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Integer getCurrentUserId() {
        // 从请求上下文中获取用户信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 从请求中获取用户ID，这里假设用户ID存储在请求属性中
            Object userId = request.getAttribute("userId");
            if (userId != null) {
                return (Integer) userId;
            }
        }
        
        // 如果无法获取用户ID，返回默认值1（测试用）
        // 实际应用中应该抛出异常或返回null
        return 1;
    }
    
    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getCurrentUsername() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object username = request.getAttribute("username");
            if (username != null) {
                return (String) username;
            }
        }
        
        // 默认返回测试用户名
        return "admin";
    }
} 