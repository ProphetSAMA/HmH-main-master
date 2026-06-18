package fun.wsss.hmh.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回结果类
 */
@Data
public class Result {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 返回码
     */
    private Integer code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * 私有构造方法
     */
    private Result() {}
    
    /**
     * 成功静态方法
     * @return 成功结果
     */
    public static Result ok() {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }
    
    /**
     * 失败静态方法
     * @return 失败结果
     */
    public static Result error() {
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage("失败");
        return r;
    }
    
    /**
     * 成功静态方法
     * @param data 数据
     * @return 成功结果
     */
    public static Result success(Object data) {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        r.data.put("data", data);
        return r;
    }
    
    /**
     * 成功静态方法
     * @param page 分页数据
     * @return 成功结果
     */
    public static <T> Result success(Page<T> page) {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        r.data.put("records", page.getRecords());
        r.data.put("total", page.getTotal());
        r.data.put("pages", page.getPages());
        r.data.put("current", page.getCurrent());
        r.data.put("size", page.getSize());
        return r;
    }
    
    /**
     * 失败静态方法
     * @param message 错误消息
     * @return 失败结果
     */
    public static Result error(String message) {
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage(message);
        return r;
    }
    
    /**
     * 失败静态方法
     * @param code 错误码
     * @param message 错误消息
     * @return 失败结果
     */
    public static Result error(Integer code, String message) {
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
    
    /**
     * 设置是否成功
     * @param success 是否成功
     * @return 当前对象
     */
    public Result success(Boolean success) {
        this.setSuccess(success);
        return this;
    }
    
    /**
     * 设置返回码
     * @param code 返回码
     * @return 当前对象
     */
    public Result code(Integer code) {
        this.setCode(code);
        return this;
    }
    
    /**
     * 设置返回消息
     * @param message 返回消息
     * @return 当前对象
     */
    public Result message(String message) {
        this.setMessage(message);
        return this;
    }
    
    /**
     * 设置返回数据
     * @param key 键
     * @param value 值
     * @return 当前对象
     */
    public Result data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
    
    /**
     * 设置返回数据
     * @param map 数据
     * @return 当前对象
     */
    public Result data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }
    
    /**
     * 成功静态方法（无参数）
     * @return 成功结果
     */
    public static Result success() {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }
}