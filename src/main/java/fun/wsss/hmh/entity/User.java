package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类
 */
@Data
@TableName("t_user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 用户名
     */
    @TableField("userName")
    private String userName;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 真实姓名
     */
    @TableField("trueName")
    private String trueName;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色名称
     */
    @TableField("roleName")
    private String roleName;
    
    /**
     * 头像
     */
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
