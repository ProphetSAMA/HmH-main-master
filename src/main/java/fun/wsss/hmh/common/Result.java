package fun.wsss.hmh.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回结果类
 */
public class Result {

    private Boolean success;
    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    private Result() {}

    // ========== getter / setter ==========

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    // ========== 静态工厂方法 ==========

    public static Result ok() {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }

    public static Result error() {
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage("失败");
        return r;
    }

    public static Result success() {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }

    public static Result success(Object data) {
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        r.data.put("data", data);
        return r;
    }

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

    public static Result error(String message) {
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    public static Result error(Integer code, String message) {
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    // ========== 链式调用方法 ==========

    public Result success(Boolean success) {
        this.setSuccess(success);
        return this;
    }

    public Result code(Integer code) {
        this.setCode(code);
        return this;
    }

    public Result message(String message) {
        this.setMessage(message);
        return this;
    }

    public Result data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public Result data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }
}
