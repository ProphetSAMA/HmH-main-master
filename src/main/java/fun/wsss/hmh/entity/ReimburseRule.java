package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 报销规则实体类
 */
@Data
@TableName("t_reimburse_rule")
public class ReimburseRule {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 费用类型ID
     */
    private Integer typeId;
    
    /**
     * 费用类型名称
     */
    private String typeName;
    
    /**
     * 最高限额
     */
    private BigDecimal maxAmount;
    
    /**
     * 预警金额
     */
    private BigDecimal warningAmount;
    
    /**
     * 规则说明
     */
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 