package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 报销类型实体类
 */
@Data
@TableName("t_type")
public class Type {
    
    /**
     * 类型ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 类型名称
     */
    private String type;
}
