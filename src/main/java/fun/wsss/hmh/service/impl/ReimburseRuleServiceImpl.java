package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.wsss.hmh.dao.ReimburseRuleDao;
import fun.wsss.hmh.entity.ReimburseRule;
import fun.wsss.hmh.service.ReimburseRuleService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 报销规则服务实现类
 */
@Service
public class ReimburseRuleServiceImpl extends ServiceImpl<ReimburseRuleDao, ReimburseRule> implements ReimburseRuleService {
    
    @Override
    public boolean updateStatus(Integer id, Integer status) {
        ReimburseRule rule = getById(id);
        if (rule == null) {
            return false;
        }
        
        rule.setStatus(status);
        rule.setUpdateTime(new Date());
        
        return updateById(rule);
    }
    
    @Override
    public Map<String, Object> checkAmount(Integer typeId, Double amount) {
        Map<String, Object> result = new HashMap<>();
        result.put("exceeded", false);
        
        // 查询对应类型的规则
        LambdaQueryWrapper<ReimburseRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReimburseRule::getTypeId, typeId)
               .eq(ReimburseRule::getStatus, 1); // 只检查启用的规则
        
        ReimburseRule rule = getOne(wrapper);
        
        // 如果没有找到规则，或者规则未启用，则不做限制
        if (rule == null) {
            return result;
        }
        
        // 检查是否超出最高限额
        BigDecimal maxAmount = rule.getMaxAmount();
        BigDecimal amountDecimal = BigDecimal.valueOf(amount);
        
        result.put("maxAmount", maxAmount);
        result.put("warningAmount", rule.getWarningAmount());
        
        // 如果超出最高限额
        if (amountDecimal.compareTo(maxAmount) > 0) {
            result.put("exceeded", true);
            result.put("message", "报销金额超出了最高限额 " + maxAmount);
        }
        // 如果超出预警金额但未超出最高限额
        else if (amountDecimal.compareTo(rule.getWarningAmount()) > 0) {
            result.put("warning", true);
            result.put("message", "报销金额接近最高限额");
        }
        
        return result;
    }

    /**
     * 保存或更新规则
     * 如果已存在相同类型的规则，则更新现有记录；否则新增
     */
    @Override
    public boolean saveOrUpdate(ReimburseRule rule) {
        // 检查是否已存在相同类型的规则
        LambdaQueryWrapper<ReimburseRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReimburseRule::getTypeId, rule.getTypeId());
        
        ReimburseRule existingRule = getOne(wrapper);
        
        if (existingRule != null) {
            // 如果存在，则更新现有记录
            rule.setId(existingRule.getId());
            rule.setUpdateTime(new Date());
            return updateById(rule);
        } else {
            // 如果不存在，则新增
            rule.setCreateTime(new Date());
            rule.setUpdateTime(new Date());
            return save(rule);
        }
    }
} 