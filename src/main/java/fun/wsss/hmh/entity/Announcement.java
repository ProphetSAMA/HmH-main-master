package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告实体类
 */
@Data
@TableName("announcement")
public class Announcement {
    
    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;
    
    /**
     * 创建人姓名
     */
    @TableField("create_user_name")
    private String createUserName;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
    
    /**
     * 状态：1-有效 0-无效
     */
    private Integer status;

    /**
     * 发布者ID
     */
    @TableField(exist = false)
    private Integer publisherId;

    /**
     * 发布者姓名
     */
    @TableField(exist = false)
    private String publisherName;
}
