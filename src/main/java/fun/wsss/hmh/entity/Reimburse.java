package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 报销实体
 *
 * @author h
 */
@Data
@TableName("reimburse")
public class Reimburse {

    /**
     * 报销id
     */
    @TableId
    private Integer id;

    /**
     * 申请人id
     */
    @TableField("sq_user_id")
    private Integer sqUserId;

    /**
     * 审批人id
     */
    @TableField("sp_user_id")
    private Integer spUserId;

    /**
     * 报销类型
     */
    @TableField("c_type")
    private Integer cType;

    /**
     * 报销金额
     */
    @TableField("money")
    private Double money;

    /**
     * 报销原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 拒绝原因
     */
    @TableField("deny_reason")
    private String denyReason;

    /**
     * 报销状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 报销类型
     */
    @TableField("type")
    private Integer type;

    /**
     * 报销类型id
     */
    @TableField("type_id")
    private Integer typeId;

    /**
     * 审批人姓名
     */
    @TableField("sp_name")
    private String spName;

    /**
     * 申请人姓名
     */
    @TableField("sq_name")
    private String sqName;

    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private Date endTime;

    public Reimburse() {
        super();
    }

    public Reimburse(Integer id, Integer sqUserId, Integer spUserId, Integer cType, Double money, Integer status,
                     String reason, String denyReason, Integer type, Integer typeId, String spName, String sqName,
                     Date createTime, Date endTime) {
        super();
        this.id = id;
        this.sqUserId = sqUserId;
        this.spUserId = spUserId;
        this.cType = cType;
        this.money = money;
        this.status = status;
        this.reason = reason;
        this.denyReason = denyReason;
        this.type = type;
        this.typeId = typeId;
        this.spName = spName;
        this.sqName = sqName;
        this.createTime = createTime;
        this.endTime = endTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSqUserId(Integer sqUserId) {
        this.sqUserId = sqUserId;
    }

    public void setSpUserId(Integer spUserId) {
        this.spUserId = spUserId;
    }

    public void setcType(Integer cType) {
        this.cType = cType;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDenyReason(String denyReason) {
        this.denyReason = denyReason;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }

    public void setSqName(String sqName) {
        this.sqName = sqName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSqUserId() {
        return sqUserId;
    }

    public Integer getSpUserId() {
        return spUserId;
    }

    public Integer getcType() {
        return cType;
    }

    public Double getMoney() {
        return money;
    }

    public Integer getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getDenyReason() {
        return denyReason;
    }

    public Integer getType() {
        return type;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public String getSpName() {
        return spName;
    }

    public String getSqName() {
        return sqName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getEndTime() {
        return endTime;
    }

}
