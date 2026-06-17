package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 发票实体类
 */
@Data
@TableName("t_invoice")
public class Invoice {
    
    /**
     * 发票ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 发票号码
     */
    @TableField("invoice_no")
    private String invoiceNo;
    
    /**
     * 发票代码
     */
    @TableField("invoice_code")
    private String invoiceCode;
    
    /**
     * 发票金额
     */
    private BigDecimal amount;
    
    /**
     * 开票日期
     */
    @TableField("invoice_date")
    private Date invoiceDate;
    
    /**
     * 销售方名称
     */
    private String seller;
    
    /**
     * 发票图片URL
     */
    @TableField("image_url")
    private String imageUrl;
    
    /**
     * 状态：0-未使用 1-已关联
     */
    private Integer status;
    
    /**
     * 关联的报销单ID
     */
    @TableField("reimburse_id")
    private Integer reimburseId;
    
    /**
     * 关联的报销单号
     */
    @TableField("reimburse_no")
    private String reimburseNo;
    
    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Integer createUserId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}