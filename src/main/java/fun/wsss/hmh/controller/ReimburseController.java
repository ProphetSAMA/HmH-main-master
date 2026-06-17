package fun.wsss.hmh.controller;

import fun.wsss.hmh.entity.PageBean;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.NotificationService;
import fun.wsss.hmh.service.ReimburseService;
import fun.wsss.hmh.service.TypeService;
import fun.wsss.hmh.service.UserService;
import fun.wsss.hmh.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报销管理控制器
 *
 * @author wsss
 */
@RestController
@RequestMapping("/api/reimburse")
public class ReimburseController {

    @Autowired
    private ReimburseService reimburseService;

    @Autowired
    private TypeService typeService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 保存或更新报销信息
     */
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Reimburse reimburse, HttpServletRequest request) {
        User user = TokenUtil.getUserFromToken(request, userService);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "用户未登录"));
        }

        // 新增报销
        if (reimburse.getId() == null) {
            return handleNewReimburse(reimburse, user);
        }

        // 更新报销
        return handleUpdateReimburse(reimburse, user);
    }

    /**
     * 删除报销信息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Reimburse reimburse = new Reimburse();
        reimburse.setId(id);
        reimburseService.delete(reimburse);
        return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
    }

    /**
     * 获取报销信息详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReimburseById(@PathVariable Integer id, HttpServletRequest request) {
        User user = TokenUtil.getUserFromToken(request, userService);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "用户未登录"));
        }

        Reimburse reimburse = new Reimburse();
        reimburse.setId(id);
        Reimburse result = reimburseService.getReimburseById(reimburse);
        return ResponseEntity.ok(Map.of("success", true, "data", result));
    }

    /**
     * 获取报销列表
     *
     * @param page    页码
     * @param rows    每页行数
     * @param type    类型
     * @param spName  审批人姓名
     * @param request 请求
     * @return 响应
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String spName,
            HttpServletRequest request) {

        User user = TokenUtil.getUserFromToken(request, userService);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "用户未登录"));
        }

        Map<String, Object> paramMap = buildListParamMap(page, rows, type, spName, user);
        List<Reimburse> reimburseList = reimburseService.list(paramMap);
        Long total = reimburseService.getTotal(paramMap);

        Map<String, Object> result = new HashMap<>(3);
        result.put("success", true);
        result.put("data", reimburseList);
        result.put("total", total);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有类型
     */
    @GetMapping("/types")
    public ResponseEntity<?> getTypes() {
        return ResponseEntity.ok(Map.of("success", true, "data", typeService.list()));
    }

    /**
     * 获取报销统计数据
     *
     * @param request 请求
     * @return 响应
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getReimburseStats(HttpServletRequest request) {
        User user = TokenUtil.getUserFromToken(request, userService);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "用户未登录"));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", reimburseService.getReimburseStats()));
    }

    /**
     * 处理新增报销请求
     *
     * @param reimburse 报销信息
     * @param user      用户
     * @return 响应
     */
    private ResponseEntity<?> handleNewReimburse(Reimburse reimburse, User user) {
        reimburse.setSqUserId(user.getId());
        reimburse.setSqName(user.getTrueName());
        if (reimburse.getStatus() == null) {
            reimburse.setStatus(3);
        }
        reimburseService.save(reimburse);

        notificationService.sendNewReimburseNotification(reimburse, user);
        return ResponseEntity.ok(Map.of("success", true, "message", "保存成功"));
    }

    /**
     * 处理更新报销请求
     *
     * @param reimburse 报销信息
     * @param user      用户
     * @return 响应
     */
    private ResponseEntity<?> handleUpdateReimburse(Reimburse reimburse, User user) {
        if ("经理".equals(user.getRoleName())) {
            reimburse.setSpUserId(user.getId());
            reimburse.setSpName(user.getTrueName());

            if (reimburse.getStatus() == 1) {
                notificationService.sendApprovalNotification(reimburse, user);
            } else if (reimburse.getStatus() == 2) {
                notificationService.sendRejectionNotification(reimburse, user);
            }
        }

        if ("员工".equals(user.getRoleName())) {
            reimburse.setStatus(3);
        }

        reimburseService.update(reimburse);
        return ResponseEntity.ok(Map.of("success", true, "message", "更新成功"));
    }

    /**
     * 构建列表查询参数
     *
     * @param page   页码
     * @param rows   每页行数
     * @param type   类型
     * @param spName 审批人姓名
     * @param user   用户
     * @return 查询参数
     */
    private Map<String, Object> buildListParamMap(int page, int rows, Integer type, String spName, User user) {
        PageBean pageBean = new PageBean(page, rows);
        Map<String, Object> paramMap = new HashMap<>(6);
        paramMap.put("start", pageBean.getStart());
        paramMap.put("size", pageBean.getPageSize());
        paramMap.put("type", type);
        paramMap.put("spName", spName);

        if ("经理".equals(user.getRoleName())) {
            paramMap.put("status", 3);
        }
        if ("员工".equals(user.getRoleName())) {
            paramMap.put("sqUserId", user.getId());
        }

        return paramMap;
    }
}
