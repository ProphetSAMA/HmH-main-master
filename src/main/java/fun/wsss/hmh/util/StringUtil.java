package fun.wsss.hmh.util;

/**
 * 字符串工具类
 */
public class StringUtil {
    
    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 获取字符串，如果为null则返回空字符串
     * @param str 字符串
     * @return 非null的字符串
     */
    public static String getString(String str) {
        return str == null ? "" : str;
    }
    
    /**
     * 格式化模糊查询
     * @param str 字符串
     * @return 格式化后的字符串
     */
    public static String formatLike(String str) {
        if (isNotEmpty(str)) {
            return "%" + str + "%";
        } else {
            return null;
        }
    }
}
