package fun.wsss.hmh.controller;

import fun.wsss.hmh.common.Result;
import fun.wsss.hmh.entity.ReimburseRule;
import fun.wsss.hmh.service.ReimburseRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报销规则控制器
 */
@RestController
@RequestMapping("/api/reimburse/rule")
@RequiredArgsConstructor
public class ReimburseRuleController {
    
    private final ReimburseRuleService reimburseRuleService;
    
    /**
     * 获取报销规则列表
     */
    @GetMapping("/list")
    public Result list() {
        try {
            List<ReimburseRule> rules = reimburseRuleService.list();
            return Result.success(rules);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("获取规则列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存报销规则
     */
    @PostMapping("/save")
    public Result save(@RequestBody ReimburseRule rule) {
        try {
            // 不需要在这里设置时间，交给Service处理
            boolean success = reimburseRuleService.saveOrUpdate(rule);
            return success ? Result.success() : Result.error().message("保存失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除报销规则
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            boolean success = reimburseRuleService.removeById(id);
            return success ? Result.success() : Result.error().message("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新规则状态
     */
    @PostMapping("/status")
    public Result updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            Integer status = (Integer) params.get("status");
            
            if (id == null || status == null) {
                return Result.error().message("参数错误");
            }
            
            boolean success = reimburseRuleService.updateStatus(id, status);
            return success ? Result.success() : Result.error().message("更新状态失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("更新状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查报销金额是否超出限额
     */
    @PostMapping("/check")
    public Result checkAmount(@RequestBody Map<String, Object> params) {
        try {
            Integer typeId = (Integer) params.get("typeId");
            Double amount = Double.parseDouble(params.get("amount").toString());
            
            if (typeId == null || amount == null) {
                return Result.error().message("参数错误");
            }
            
            Map<String, Object> result = reimburseRuleService.checkAmount(typeId, amount);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("检查金额失败: " + e.getMessage());
        }
    }
} 