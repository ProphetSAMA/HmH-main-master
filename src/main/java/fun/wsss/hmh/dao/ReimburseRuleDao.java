package fun.wsss.hmh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.ReimburseRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销规则DAO接口
 */
@Mapper
public interface ReimburseRuleDao extends BaseMapper<ReimburseRule> {
} 