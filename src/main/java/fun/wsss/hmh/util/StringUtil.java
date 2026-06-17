package fun.wsss.hmh.util;

/**
 * 字符串工具类
 * @author
 *
 */
public class StringUtil {

	/**
	 * 判断是否是空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		if(str==null||"".equals(str.trim())){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 判断是否是空
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str){
		if((str!=null)&&!"".equals(str.trim())){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 格式化模糊查询（防SQL注入）
	 * @param str
	 * @return
	 */
	public static String formatLike(String str){
		if(isNotEmpty(str)){
			// 转义SQL特殊字符，防止SQL注入
			String escaped = str.replace("\\", "\\\\")
					.replace("%", "\\%")
					.replace("_", "\\_")
					.replace("'", "\\'")
					.replace("\"", "\\\"")
					.replace(";", "\\;")
					.replace("--", "\\--")
					.replace("/*", "\\/*")
					.replace("*/", "\\*/");
			return "%"+escaped+"%";
		}else{
			return null;
		}
	}

	/**
	 * 清理字符串中的SQL注入字符
	 * @param str 输入字符串
	 * @return 清理后的字符串
	 */
	public static String sanitizeSql(String str){
		if(isEmpty(str)){
			return str;
		}
		// 移除或转义危险字符
		return str.replace("'", "''")
				.replace(";", "")
				.replace("--", "")
				.replace("/*", "")
				.replace("*/", "")
				.replace("xp_", "")
				.replace("exec", "")
				.replace("execute", "")
				.replace("insert", "")
				.replace("delete", "")
				.replace("update", "")
				.replace("drop", "")
				.replace("truncate", "")
				.replace("declare", "")
				.replace("master", "")
				.replace("script", "");
	}

	/**
	 * 验证字符串是否只包含安全字符
	 * @param str 输入字符串
	 * @return true表示安全
	 */
	public static boolean isSafeString(String str){
		if(isEmpty(str)){
			return true;
		}
		// 检查是否包含危险字符
		String lowerStr = str.toLowerCase();
		String[] dangerousPatterns = {
				"'", "\"", ";", "--", "/*", "*/",
				"exec", "execute", "insert", "delete",
				"update", "drop", "truncate", "declare",
				"master", "script", "xp_", "union",
				"select", "from", "where", "or ", "and "
		};

		for(String pattern : dangerousPatterns){
			if(lowerStr.contains(pattern)){
				return false;
			}
		}
		return true;
	}
}
