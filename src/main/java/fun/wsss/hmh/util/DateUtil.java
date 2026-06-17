package fun.wsss.hmh.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {
    
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 格式化日期
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_PATTERN);
    }
    
    /**
     * 格式化日期
     * @param date 日期
     * @param pattern 格式
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    /**
     * 解析日期
     * @param dateStr 日期字符串
     * @return 解析后的日期
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_PATTERN);
    }
    
    /**
     * 解析日期
     * @param dateStr 日期字符串
     * @param pattern 格式
     * @return 解析后的日期
     */
    public static Date parseDate(String dateStr, String pattern) {
        if (StringUtil.isEmpty(dateStr)) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
