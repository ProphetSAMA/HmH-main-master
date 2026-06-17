package fun.wsss.hmh.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 * 使用ThreadLocal保证线程安全
 */
public class DateUtil {

	// 使用ThreadLocal存储SimpleDateFormat，保证线程安全
	private static final ThreadLocal<SimpleDateFormat> SDF_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat();
		}
	};

	/**
	 * 格式化日期
	 * @param date 日期
	 * @param format 格式
	 * @return 格式化后的字符串
	 */
	public static String formatDate(Date date, String format){
		if(date == null){
			return "";
		}
		SimpleDateFormat sdf = SDF_THREAD_LOCAL.get();
		sdf.applyPattern(format);
		return sdf.format(date);
	}

	/**
	 * 解析日期字符串
	 * @param str 日期字符串
	 * @param format 格式
	 * @return Date对象
	 * @throws Exception 解析异常
	 */
	public static Date formatString(String str, String format) throws Exception{
		if(StringUtil.isEmpty(str)){
			return null;
		}
		SimpleDateFormat sdf = SDF_THREAD_LOCAL.get();
		sdf.applyPattern(format);
		return sdf.parse(str);
	}

	/**
	 * 获取当前日期字符串
	 * @return 日期字符串
	 */
	public static String getCurrentDateStr(){
		return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	}

	/**
	 * 获取当前日期时间字符串
	 * @return 日期时间字符串
	 */
	public static String getCurrentDateTimeStr(){
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}

	/**
	 * 格式化LocalDate
	 * @param date LocalDate对象
	 * @param format 格式
	 * @return 格式化后的字符串
	 */
	public static String formatLocalDate(LocalDate date, String format){
		if(date == null){
			return "";
		}
		return date.format(DateTimeFormatter.ofPattern(format));
	}

	/**
	 * 格式化LocalDateTime
	 * @param dateTime LocalDateTime对象
	 * @param format 格式
	 * @return 格式化后的字符串
	 */
	public static String formatLocalDateTime(LocalDateTime dateTime, String format){
		if(dateTime == null){
			return "";
		}
		return dateTime.format(DateTimeFormatter.ofPattern(format));
	}

	/**
	 * 清理ThreadLocal资源
	 * 在应用关闭时调用，防止内存泄漏
	 */
	public static void clearThreadLocal(){
		SDF_THREAD_LOCAL.remove();
	}
}
