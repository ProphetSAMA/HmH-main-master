package fun.wsss.hmh.utils;

import fun.wsss.hmh.entity.User;

/**
 * 用户上下文工具类
 */
public class UserContext {
    
    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     * @param user 用户信息
     */
    public static void setCurrentUser(User user) {
        userThreadLocal.set(user);
    }
    
    /**
     * 获取当前用户
     * @return 当前用户
     */
    public static User getCurrentUser() {
        return userThreadLocal.get();
    }
    
    /**
     * 清除当前用户
     */
    public static void clear() {
        userThreadLocal.remove();
    }
} 