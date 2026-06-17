package fun.wsss.hmh.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 * 提供密码加密和验证功能
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 生成随机盐值
     * @return Base64编码的盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用SHA-256加密密码
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));

            byte[] hash = md.digest(password.getBytes());

            // 迭代加密
            for (int i = 0; i < ITERATIONS; i++) {
                md.reset();
                hash = md.digest(hash);
            }

            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 验证密码
     * @param password 原始密码
     * @param storedHash 存储的加密密码
     * @param salt 盐值
     * @return true表示密码正确
     */
    public static boolean verifyPassword(String password, String storedHash, String salt) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(storedHash);
    }

    /**
     * 生成加密后的密码（包含盐值）
     * @param password 原始密码
     * @return 格式：salt:hash
     */
    public static String encryptPassword(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + ":" + hash;
    }

    /**
     * 验证密码（使用存储的加密密码）
     * @param password 原始密码
     * @param storedPassword 存储的加密密码（格式：salt:hash）
     * @return true表示密码正确
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        if (storedPassword == null || !storedPassword.contains(":")) {
            return false;
        }

        String[] parts = storedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }

        String salt = parts[0];
        String storedHash = parts[1];

        return verifyPassword(password, storedHash, salt);
    }

    /**
     * 检查密码强度
     * @param password 密码
     * @return true表示密码符合要求
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }

        // 至少包含大写字母、小写字母、数字中的两种
        int count = 0;
        if (hasUpperCase) count++;
        if (hasLowerCase) count++;
        if (hasDigit) count++;
        if (hasSpecialChar) count++;

        return count >= 2;
    }
}
