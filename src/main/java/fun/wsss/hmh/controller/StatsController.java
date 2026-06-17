package fun.wsss.hmh.controller;

import fun.wsss.hmh.common.Result;
import fun.wsss.hmh.service.ReimburseService;
import fun.wsss.hmh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    
    private final ReimburseService reimburseService;
    private final UserService userService;
    
    /**
     * 获取统计摘要数据
     */
    @GetMapping("/summary")
    public Result getSummary() {
        Map<String, Object> data = new HashMap<>();
        
        // 获取总报销金额
        BigDecimal totalAmount = reimburseService.getTotalAmount();
        data.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        // 获取待审批报销数量
        Integer pendingCount = reimburseService.getPendingCount();
        data.put("pendingCount", pendingCount != null ? pendingCount : 0);
        
        // 获取已拒绝报销数量
        Integer rejectedCount = reimburseService.getRejectedCount();
        data.put("rejectedCount", rejectedCount != null ? rejectedCount : 0);
        
        // 获取已批准报销数量
        Integer approvedCount = reimburseService.getApprovedCount();
        data.put("approvedCount", approvedCount != null ? approvedCount : 0);
        
        // 获取本月报销人数
        Integer monthlyUserCount = reimburseService.getMonthlyUserCount();
        data.put("monthlyUserCount", monthlyUserCount != null ? monthlyUserCount : 0);
        
        // 获取同比增长率
        Double growthRate = reimburseService.getGrowthRate();
        data.put("growthRate", growthRate != null ? growthRate : 0.0);
        
        return Result.success(data);
    }
    
    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    public Result getTrend() {
        // 获取最近6个月的数据
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthStr);
            
            // 获取该月的报销金额
            BigDecimal amount = reimburseService.getMonthlyAmount(month);
            monthData.put("amount", amount != null ? amount : BigDecimal.ZERO);
            
            // 获取该月的报销数量
            Integer count = reimburseService.getMonthlyCount(month);
            monthData.put("count", count != null ? count : 0);
            
            trendData.add(monthData);
        }
        
        return Result.success(trendData);
    }
    
    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 待处理报销数量
            int pendingCount = reimburseService.getPendingCount();
            stats.put("pendingCount", pendingCount);
            
            // 本月报销总额
            BigDecimal monthlyAmount = reimburseService.getMonthlyAmount();
            stats.put("monthlyAmount", monthlyAmount);
            
            // 系统用户数
            int userCount = userService.getUserCount();
            stats.put("userCount", userCount);
            
            // 未读消息数量 (示例数据)
            stats.put("unreadCount", 0);
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取月度报销统计数据
     */
    @GetMapping("/monthly")
    public Result getMonthlyStats() {
        try {
            // 这里应该从数据库获取实际数据
            // 为了演示，我们使用模拟数据
            List<Map<String, Object>> data = new ArrayList<>();
            
            // 生成最近6个月的数据
            Calendar calendar = Calendar.getInstance();
            for (int i = 5; i >= 0; i--) {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - (i == 5 ? 0 : 1));
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", String.format("%d-%02d", 
                    calendar.get(Calendar.YEAR), 
                    calendar.get(Calendar.MONTH) + 1));
                
                // 生成1000-5000之间的随机金额
                double amount = 1000 + Math.random() * 4000;
                monthData.put("amount", Math.round(amount * 100) / 100.0);
                
                data.add(monthData);
            }
            
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("获取月度统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取报销类别统计数据
     */
    @GetMapping("/category")
    public Result getCategoryStats() {
        try {
            // 这里应该从数据库获取实际数据
            // 为了演示，我们使用模拟数据
            List<Map<String, Object>> data = new ArrayList<>();
            
            // 添加各类别数据
            String[] categories = {"车补", "餐补", "交通补", "住宿"};
            for (String category : categories) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("name", category);
                
                // 生成500-3000之间的随机金额
                double value = 500 + Math.random() * 2500;
                categoryData.put("value", Math.round(value * 100) / 100.0);
                
                data.add(categoryData);
            }
            
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("获取类别统计数据失败: " + e.getMessage());
        }
    }
} 