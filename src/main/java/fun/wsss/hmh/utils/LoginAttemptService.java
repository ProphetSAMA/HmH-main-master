package fun.wsss.hmh.utils;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录尝试限制服务
 * 防止暴力破解攻击
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15分钟

    // 存储登录尝试次数和时间
    private final ConcurrentHashMap<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    /**
     * 登录尝试记录
     */
    private static class LoginAttempt {
        private final AtomicInteger attempts;
        private long lastAttemptTime;

        public LoginAttempt() {
            this.attempts = new AtomicInteger(0);
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public AtomicInteger getAttempts() {
            return attempts;
        }

        public long getLastAttemptTime() {
            return lastAttemptTime;
        }

        public void setLastAttemptTime(long lastAttemptTime) {
            this.lastAttemptTime = lastAttemptTime;
        }
    }

    /**
     * 记录登录失败
     * @param key 用户名或IP地址
     */
    public void loginFailed(String key) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(key, k -> new LoginAttempt());
        attempt.getAttempts().incrementAndGet();
        attempt.setLastAttemptTime(System.currentTimeMillis());
    }

    /**
     * 登录成功，清除尝试记录
     * @param key 用户名或IP地址
     */
    public void loginSucceeded(String key) {
        loginAttempts.remove(key);
    }

    /**
     * 检查是否被锁定
     * @param key 用户名或IP地址
     * @return true表示被锁定
     */
    public boolean isBlocked(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null) {
            return false;
        }

        // 检查是否超过最大尝试次数
        if (attempt.getAttempts().get() >= MAX_ATTEMPTS) {
            // 检查锁定时间是否已过
            long timeSinceLastAttempt = System.currentTimeMillis() - attempt.getLastAttemptTime();
            if (timeSinceLastAttempt < LOCK_TIME_DURATION) {
                return true;
            } else {
                // 锁定时间已过，清除记录
                loginAttempts.remove(key);
                return false;
            }
        }

        return false;
    }

    /**
     * 获取剩余锁定时间（秒）
     * @param key 用户名或IP地址
     * @return 剩余锁定时间，如果未锁定返回0
     */
    public int getRemainingLockTime(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null || attempt.getAttempts().get() < MAX_ATTEMPTS) {
            return 0;
        }

        long timeSinceLastAttempt = System.currentTimeMillis() - attempt.getLastAttemptTime();
        long remainingTime = LOCK_TIME_DURATION - timeSinceLastAttempt;

        return remainingTime > 0 ? (int) (remainingTime / 1000) : 0;
    }

    /**
     * 获取剩余尝试次数
     * @param key 用户名或IP地址
     * @return 剩余尝试次数
     */
    public int getRemainingAttempts(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }

        return Math.max(0, MAX_ATTEMPTS - attempt.getAttempts().get());
    }
}
