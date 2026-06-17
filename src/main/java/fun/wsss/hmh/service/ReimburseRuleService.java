package fun.wsss.hmh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.wsss.hmh.entity.ReimburseRule;

import java.util.Map;

/**
 * 报销规则服务接口
 */
public interface ReimburseRuleService extends IService<ReimburseRule> {
    
    /**
     * 更新规则状态
     * @param id 规则ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Integer id, Integer status);
    
    /**
     * 检查报销金额是否超出限额
     * @param typeId 费用类型ID
     * @param amount 金额
     * @return 检查结果
     */
    Map<String, Object> checkAmount(Integer typeId, Double amount);
} 