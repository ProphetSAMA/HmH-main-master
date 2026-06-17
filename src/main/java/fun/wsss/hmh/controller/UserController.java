package fun.wsss.hmh.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fun.wsss.hmh.entity.PageBean;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.UserService;
import fun.wsss.hmh.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理 Controller
 *
 * @author h
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final String SECRET_KEY = "your-secret-key";

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     *
     * @param user 登录用户信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        // 参数校验
        if (StringUtil.isEmpty(user.getUserName())) {
            response.put("success", false);
            response.put("message", "用户名为空！");
            return ResponseEntity.badRequest().body(response);
        }
        if (StringUtil.isEmpty(user.getPassword())) {
            response.put("success", false);
            response.put("message", "密码为空！");
            return ResponseEntity.badRequest().body(response);
        }
        if (StringUtil.isEmpty(user.getRoleName())) {
            response.put("success", false);
            response.put("message", "请选择角色类型！");
            return ResponseEntity.badRequest().body(response);
        }

        // 登录逻辑
        User resultUser = userService.login(user);
        if (resultUser == null) {
            response.put("success", false);
            response.put("message", "用户名或密码错误！");
            return ResponseEntity.status(401).body(response);
        }

        // 生成 token（使用用户ID和角色信息）
        String token = JWT.create()
                .withClaim("userId", resultUser.getId())
                .withClaim("userName", resultUser.getUserName())
                .withClaim("roleName", resultUser.getRoleName())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24小时过期
                .sign(Algorithm.HMAC256(SECRET_KEY));

        // 返回登录结果
        response.put("success", true);
        response.put("message", "登录成功！");
        response.put("token", token);
        response.put("userId", resultUser.getId());
        response.put("userName", resultUser.getUserName());
        response.put("roleName", resultUser.getRoleName());
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注销接口
     *
     * @return 注销结果
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "注销成功！"));
    }

    /**
     * 用户分页查询接口
     *
     * @param page     当前页码
     * @param rows     每页条数
     * @param roleName 角色名（可选）
     * @param userName 用户名（可选）
     * @return 用户分页数据
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "rows", defaultValue = "5") int rows,
            @RequestParam(value = "roleName", required = false) String roleName,
            @RequestParam(value = "userName", required = false) String userName) {

        PageBean pageBean = new PageBean(page, rows);
        Map<String, Object> map = new HashMap<>();
        map.put("userName", StringUtil.formatLike(userName));
        map.put("roleName", roleName);
        map.put("start", pageBean.getStart());
        map.put("size", pageBean.getPageSize());

        List<User> userList = userService.find(map);
        Long total = userService.getTotal(map);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", userList);
        result.put("total", total);

        return ResponseEntity.ok(result);
    }

    /**
     * 用户修改密码
     *
     * @param id          用户 ID
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/modifyPassword")
    public ResponseEntity<?> modifyPassword(
            @RequestParam("id") Integer id,
            @RequestParam("newPassword") String newPassword) {

        // 从数据库获取当前用户信息
        User currentUser = userService.findById(id);

        if (currentUser == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "用户未找到"));
        }

        // 更新密码，并保留其他字段
        currentUser.setPassword(newPassword);

        int result = userService.update(currentUser);
        if (result > 0) {
            return ResponseEntity.ok(Map.of("success", true, "message", "密码修改成功"));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "密码修改失败"));
        }
    }

    /**
     * 删除用户
     *
     * @param ids 用户 ID 列表
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam("ids") List<Integer> ids) {
        if (ids.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "参数错误"));
        }
        ids.forEach(userService::delete);
        return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
    }

    /**
     * 获取指定用户信息
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        // 通过 userService 获取用户信息
        User user = userService.getUserById(id);

        // 如果找不到用户，则返回错误信息
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "用户不存在"));
        }

        return ResponseEntity.ok(Map.of("success", true, "data", user));
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody User user) {
        if (user.getId() == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "用户ID不能为空"));
        }

        User existingUser = userService.findById(user.getId());
        if (existingUser == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "用户不存在"));
        }

        // 只更新允许的字段
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        int result = userService.update(existingUser);
        if (result > 0) {
            return ResponseEntity.ok(Map.of("success", true, "message", "更新成功"));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "更新失败"));
        }
    }
}
