package fun.wsss.hmh.common.exception;

/**
 * 基础异常类
 */
public class BaseException extends RuntimeException {
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 构造函数
     * @param message 错误消息
     */
    public BaseException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }
    
    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误消息
     */
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取错误码
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 设置错误码
     * @param code 错误码
     */
    public void setCode(Integer code) {
        this.code = code;
    }
    
    /**
     * 获取错误消息
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置错误消息
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
} 