package fun.wsss.hmh.common.exception;

/**
 * 未授权异常类
 */
public class UnauthorizedException extends BaseException {
    
    /**
     * 构造函数
     * @param message 错误消息
     */
    public UnauthorizedException(String message) {
        super(401, message);
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param cause 异常原因
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(401, message, cause);
    }
} 