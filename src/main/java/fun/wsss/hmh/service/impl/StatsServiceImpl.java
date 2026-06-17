package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.entity.Type;
import fun.wsss.hmh.service.ReimburseService;
import fun.wsss.hmh.service.StatsService;
import fun.wsss.hmh.service.TypeService;
import fun.wsss.hmh.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Service
public class StatsServiceImpl implements StatsService {
    
    @Autowired
    private ReimburseService reimburseService;
    
    @Autowired
    private TypeService typeService;
    
    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前用户ID
        Integer userId = UserUtil.getCurrentUserId();
        
        // 查询当前用户的报销单
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getSqUserId, userId);
        
        List<Reimburse> list = reimburseService.list(wrapper);
        
        // 计算总报销金额
        double totalAmount = list.stream()
                .filter(r -> r.getStatus() == 1) // 已通过的报销单
                .mapToDouble(Reimburse::getMoney)
                .sum();
        
        // 计算待审核报销单数量
        long pendingCount = list.stream()
                .filter(r -> r.getStatus() == 3) // 待审核的报销单
                .count();
        
        // 计算已通过报销单数量
        long approvedCount = list.stream()
                .filter(r -> r.getStatus() == 1) // 已通过的报销单
                .count();
        
        // 计算已拒绝报销单数量
        long rejectedCount = list.stream()
                .filter(r -> r.getStatus() == 2) // 已拒绝的报销单
                .count();
        
        result.put("totalAmount", totalAmount);
        result.put("pendingCount", pendingCount);
        result.put("approvedCount", approvedCount);
        result.put("rejectedCount", rejectedCount);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getExpenseTrend(String timeRange) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前用户ID
        Integer userId = UserUtil.getCurrentUserId();
        
        // 确定时间范围
        Date startDate;
        Date endDate = new Date();
        LocalDate now = LocalDate.now();
        
        switch (timeRange) {
            case "week":
                startDate = Date.from(now.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            case "month":
                startDate = Date.from(now.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            case "year":
                startDate = Date.from(now.minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            default:
                startDate = Date.from(now.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
        }
        
        // 查询指定时间范围内的报销单
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getSqUserId, userId)
               .eq(Reimburse::getStatus, 1) // 已通过的报销单
               .ge(Reimburse::getCreateTime, startDate)
               .le(Reimburse::getCreateTime, endDate);
        
        List<Reimburse> list = reimburseService.list(wrapper);
        
        // 按日期分组统计金额
        Map<String, Double> dateAmountMap = new HashMap<>();
        DateTimeFormatter formatter;
        
        if ("week".equals(timeRange) || "month".equals(timeRange)) {
            formatter = DateTimeFormatter.ofPattern("MM-dd");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        }
        
        for (Reimburse reimburse : list) {
            LocalDate date = reimburse.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String dateStr = date.format(formatter);
            
            dateAmountMap.put(dateStr, dateAmountMap.getOrDefault(dateStr, 0.0) + reimburse.getMoney());
        }
        
        // 构建结果
        List<String> dates = new ArrayList<>(dateAmountMap.keySet());
        Collections.sort(dates);
        
        List<Double> amounts = dates.stream()
                .map(dateAmountMap::get)
                .collect(Collectors.toList());
        
        result.put("dates", dates);
        result.put("amounts", amounts);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getExpenseTypes() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前用户ID
        Integer userId = UserUtil.getCurrentUserId();
        
        // 查询当前用户的已通过报销单
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getSqUserId, userId)
               .eq(Reimburse::getStatus, 1); // 已通过的报销单
        
        List<Reimburse> list = reimburseService.list(wrapper);
        
        // 获取所有报销类型
        List<Type> types = typeService.getAllTypes();
        Map<Integer, String> typeMap = types.stream()
                .collect(Collectors.toMap(Type::getId, Type::getType));
        
        // 按类型分组统计金额
        Map<Integer, Double> typeAmountMap = new HashMap<>();
        
        for (Reimburse reimburse : list) {
            Integer typeId = reimburse.getTypeId();
            typeAmountMap.put(typeId, typeAmountMap.getOrDefault(typeId, 0.0) + reimburse.getMoney());
        }
        
        // 构建结果
        List<String> typeNames = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        
        for (Map.Entry<Integer, Double> entry : typeAmountMap.entrySet()) {
            Integer typeId = entry.getKey();
            Double amount = entry.getValue();
            
            typeNames.add(typeMap.getOrDefault(typeId, "未知类型"));
            amounts.add(amount);
        }
        
        result.put("types", typeNames);
        result.put("amounts", amounts);
        
        return result;
    }
} 