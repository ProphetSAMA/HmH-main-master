package fun.wsss.hmh.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体
 */
@Getter
@Setter
@TableName("t_user")
public class User {

    @TableId
    private Integer id; // 编号

    @TableField("userName")
    private String userName; // 用户名

    @TableField("password")
    private String password; // 密码

    @TableField("trueName")
    private String trueName; // 真实姓名

    @TableField("email")
    private String email; // 邮件

    @TableField("phone")
    private String phone; // 联系电话

    @TableField("roleName")
    private String roleName; // 角色名称 系统管理员 经理 员工

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
