package fun.wsss.hmh.common.exception;

/**
 * 资源不存在异常类
 */
public class NotFoundException extends BaseException {
    
    /**
     * 构造函数
     * @param message 错误消息
     */
    public NotFoundException(String message) {
        super(404, message);
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param cause 异常原因
     */
    public NotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
} 