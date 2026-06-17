package fun.wsss.hmh.service;

import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatsService {
    
    /**
     * 获取统计摘要数据
     * @return 统计摘要数据
     */
    Map<String, Object> getSummary();
    
    /**
     * 获取费用趋势数据
     * @param timeRange 时间范围
     * @return 费用趋势数据
     */
    Map<String, Object> getExpenseTrend(String timeRange);
    
    /**
     * 获取费用类型分布数据
     * @return 费用类型分布数据
     */
    Map<String, Object> getExpenseTypes();
} 